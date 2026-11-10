/** Product catalogue backed by Spring Data Page endpoints. */
const API = '/api';
const PAGE_SIZE = 9;
const money = new Intl.NumberFormat('pl-PL', { style: 'currency', currency: 'PLN' });
const escapeText = (value) => String(value ?? '').replace(/[&<>"']/g, (char) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
})[char]);
const numberId = (value) => /^[1-9]\d*$/.test(String(value ?? '')) ? String(value) : null;

async function fetchJson(url, signal) {
    const response = await fetch(url, { signal });
    if (!response.ok) {
        if (response.status === 404) throw new Error('Nie znaleziono wskazanego zasobu.');
        throw new Error(`Nie udało się pobrać danych (HTTP ${response.status}).`);
    }
    return response.json();
}

function formatPrice(value) {
    const price = Number(value);
    return value != null && Number.isFinite(price) ? money.format(price) : 'Cena niepodana';
}

function productCard(product) {
    const id = numberId(product.id);
    const title = escapeText(product.title || 'Produkt bez nazwy');
    const description = escapeText(product.description || 'Brak opisu produktu.');
    return `<article class="product-card">
        <div class="product-art" aria-hidden="true"><span>♻</span></div>
        <div class="product-card-body">
            <p class="product-caption">Produkt #${escapeText(product.id)}</p>
            <h3>${id ? `<a href="#product/${id}">${title}</a>` : title}</h3>
            <p class="product-excerpt">${description}</p>
            <div class="product-card-footer"><strong>${formatPrice(product.price)}</strong>
                ${id ? `<a class="product-link" href="#product/${id}">Zobacz szczegóły →</a>` : ''}
            </div>
        </div>
    </article>`;
}

function parseRoute() {
    const hash = decodeURIComponent(location.hash.slice(1));
    const detail = /^product\/(\d+)$/.exec(hash);
    if (detail) return { type: 'detail', id: numberId(detail[1]) };
    const params = new URLSearchParams(hash.startsWith('products?') ? hash.slice(9) : '');
    return {
        type: 'list',
        category: numberId(params.get('category')),
        seller: numberId(params.get('seller')),
        page: Math.max(0, Math.min(10000, Number.parseInt(params.get('page'), 10) || 0))
    };
}

function catalogueHash({ category, seller, page = 0 }) {
    const params = new URLSearchParams();
    if (category) params.set('category', category);
    if (seller) params.set('seller', seller);
    if (page) params.set('page', String(page));
    return `#products${params.size ? `?${params}` : ''}`;
}

export function mountProducts(container) {
    let requestNumber = 0;
    let controller;
    let categoriesCache = null;

    async function render() {
        controller?.abort();
        controller = new AbortController();
        const signal = controller.signal;
        const currentRequest = ++requestNumber;
        const route = parseRoute();
        container.innerHTML = '<p class="catalogue-status" role="status">Ładowanie produktów…</p>';
        try {
            if (route.type === 'detail') {
                if (!route.id) throw new Error('Nieprawidłowy identyfikator produktu.');
                const product = await fetchJson(`${API}/products/${route.id}`, signal);
                const categoryIds = Array.from(product.categoryIds || []);
                const categoryNames = await Promise.all(categoryIds.map(async (id) => {
                    try {
                        const category = await fetchJson(`${API}/categories/${id}`, signal);
                        return { id, name: category.name };
                    } catch (error) {
                        if (error.name === 'AbortError') throw error;
                        return { id, name: `Kategoria #${id}` };
                    }
                }));
                let sellerName = `Użytkownik #${product.sellerId}`;
                if (numberId(product.sellerId)) {
                    try {
                        const user = await fetchJson(`${API}/users/${product.sellerId}`, signal);
                        sellerName = [user.firstName, user.lastName].filter(Boolean).join(' ') || sellerName;
                    } catch (error) { if (error.name === 'AbortError') throw error; }
                }
                if (currentRequest !== requestNumber) return;
                container.innerHTML = `<div class="product-detail">
                    <a class="back-link" href="#products">← Wróć do listy produktów</a>
                    <div class="product-detail-grid">
                        <div class="product-art product-art-large" aria-hidden="true">♻</div>
                        <div><p class="eyebrow">Produkt #${escapeText(product.id)}</p>
                            <h2>${escapeText(product.title || 'Produkt bez nazwy')}</h2>
                            <p class="detail-price">${formatPrice(product.price)}</p>
                            <h3>Opis</h3><p class="detail-description">${escapeText(product.description || 'Brak opisu produktu.')}</p>
                            <h3>Kategorie</h3><div class="product-tags">${categoryNames.length ? categoryNames.map(({id, name}) => `<a href="${catalogueHash({ category: numberId(id) })}">${escapeText(name)}</a>`).join('') : '<span>Brak kategorii</span>'}</div>
                            <h3>Sprzedawca</h3><a class="seller-link" href="${catalogueHash({ seller: numberId(product.sellerId) })}">${escapeText(sellerName)} → Zobacz produkty</a>
                        </div>
                    </div>
                </div>`;
                return;
            }
            const categoriesPromise = categoriesCache ? Promise.resolve(categoriesCache) : fetchJson(`${API}/categories?size=100&sort=name,asc`, signal)
                .then((page) => { categoriesCache = page.content || []; return categoriesCache; });
            // Existing API offers separate category and seller endpoints, not their intersection.
            const endpoint = route.category ? `products/category/${route.category}` : route.seller ? `products/user/${route.seller}` : 'products';
            const url = `${API}/${endpoint}?page=${route.page}&size=${PAGE_SIZE}&sort=id,desc`;
            const [page, categoriesResult] = await Promise.allSettled([fetchJson(url, signal), categoriesPromise]);
            if (page.status === 'rejected') throw page.reason;
            const categories = categoriesResult.status === 'fulfilled' ? categoriesResult.value : [];
            if (currentRequest !== requestNumber) return;
            const items = page.value.content || [];
            const shown = items;
            container.innerHTML = `<section class="catalogue" aria-labelledby="catalogue-heading">
                <div class="catalogue-heading"><div><h2 id="catalogue-heading">Przeglądaj produkty</h2></div>
                    <span class="catalogue-count">${page.value.totalElements ?? items.length} produktów</span></div>
                <form class="catalogue-filters" id="catalogue-filters">
                    <label>Kategoria<select name="category"><option value="">Wszystkie kategorie</option>${categories.map((category) => `<option value="${escapeText(category.id)}" ${String(category.id) === route.category ? 'selected' : ''}>${escapeText(category.name)}</option>`).join('')}</select></label>
                    <label>ID sprzedawcy<input name="seller" type="number" min="1" step="1" inputmode="numeric" placeholder="Wszyscy sprzedawcy" value="${route.seller || ''}"></label>
                    <button class="filter-button" type="submit">Filtruj</button>
                    <a class="reset-link" href="#products">Wyczyść</a>
                </form>
                ${categoriesResult.status === 'rejected' ? '<p class="filter-notice">Nie udało się załadować listy kategorii.</p>' : ''}
                ${route.category && route.seller ? '<p class="filter-notice">Backend nie obsługuje jednoczesnego filtrowania według kategorii i sprzedawcy. Wyświetlono wyniki kategorii; wybierz tylko jeden filtr.</p>' : ''}
                ${shown.length ? `<div class="products-grid">${shown.map(productCard).join('')}</div>` : '<p class="catalogue-empty">Brak produktów spełniających podane kryteria.</p>'}
                <nav class="catalogue-pages" aria-label="Paginacja produktów">
                    ${route.page > 0 ? `<a class="text-button" href="${catalogueHash({ ...route, page: route.page - 1 })}">← Poprzednia</a>` : '<span></span>'}
                    <span>Strona ${route.page + 1} z ${Math.max(1, page.value.totalPages || 1)}</span>
                    ${route.page + 1 < (page.value.totalPages || 0) ? `<a class="text-button" href="${catalogueHash({ ...route, page: route.page + 1 })}">Następna →</a>` : '<span></span>'}
                </nav>
            </section>`;
            container.querySelector('#catalogue-filters').addEventListener('submit', (event) => {
                event.preventDefault();
                const data = new FormData(event.currentTarget);
                const category = numberId(data.get('category'));
                const sellerRaw = String(data.get('seller') || '').trim();
                const seller = numberId(sellerRaw);
                if (sellerRaw && !seller) {
                    event.currentTarget.elements.seller.setCustomValidity('Podaj dodatnie ID sprzedawcy.');
                    event.currentTarget.reportValidity();
                    event.currentTarget.elements.seller.setCustomValidity('');
                    return;
                }
                if (category && seller) {
                    const field = event.currentTarget.elements.seller;
                    field.setCustomValidity("Wybierz kategorię lub sprzedawcę, nie oba filtry jednocześnie.");
                    event.currentTarget.reportValidity();
                    field.setCustomValidity("");
                    return;
                }
                location.hash = catalogueHash({ category, seller });
                if (location.hash === catalogueHash(route)) render();
            });
        } catch (error) {
            if (error.name === 'AbortError' || currentRequest !== requestNumber) return;
            container.replaceChildren();
            const message = document.createElement('p');
            message.className = 'catalogue-status';
            message.setAttribute('role', 'alert');
            message.textContent = `${error.message} Spróbuj ponownie.`;
            const retry = document.createElement('button');
            retry.className = 'text-button';
            retry.textContent = 'Ponów próbę';
            retry.addEventListener('click', render);
            container.append(message, retry);
        }
    }
    window.addEventListener('hashchange', render);
    render();
    return { refresh: render, destroy() { controller?.abort(); window.removeEventListener('hashchange', render); } };
}
