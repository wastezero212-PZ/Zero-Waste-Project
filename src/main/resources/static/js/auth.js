const TOKEN_STORAGE_KEY = "zero-waste.auth-token";
const AUTH_API = "/api/auth";
let nextAuthFormId = 0;

const authMarkup = `
    <section class="auth-panel" aria-labelledby="auth-title">
        <div class="auth-panel-header">
            <div>
                <h2 id="auth-title">Welcome back</h2>
                <p data-auth-description>Sign in to continue.</p>
            </div>
            <button class="auth-close" type="button" aria-label="Close sign-in form">&times;</button>
        </div>
        <form class="auth-form" data-auth-form="login">
            <label class="auth-field">
                Email
                <input name="email" type="email" autocomplete="email" required>
            </label>
            <label class="auth-field">
                Password
                <input name="password" type="password" autocomplete="current-password" required>
            </label>
            <button class="auth-submit" type="submit">Sign in</button>
        </form>
        <form class="auth-form" data-auth-form="register" hidden>
            <label class="auth-field">
                First name
                <input name="firstName" type="text" autocomplete="given-name" required>
            </label>
            <label class="auth-field">
                Last name
                <input name="lastName" type="text" autocomplete="family-name" required>
            </label>
            <label class="auth-field">
                Email
                <input name="email" type="email" autocomplete="email" required>
            </label>
            <label class="auth-field">
                Password
                <input name="password" type="password" autocomplete="new-password"
                       minlength="8" maxlength="20" required>
            </label>
            <label class="auth-field">
                Repeat password
                <input name="repeatPassword" type="password" autocomplete="new-password"
                       minlength="8" maxlength="20" required>
            </label>
            <button class="auth-submit" type="submit">Create account</button>
        </form>
        <p class="auth-switch">
            <span data-auth-switch-label>New here?</span>
            <button type="button" data-auth-switch>Register</button>
        </p>
        <p class="auth-status" data-auth-status role="status" aria-live="polite"></p>
    </section>
`;

function getErrorMessage(body, statusText) {
    if (typeof body?.errors === "string") {
        return body.errors;
    }
    if (Array.isArray(body?.errors)) {
        return body.errors.join(" ");
    }
    if (typeof body?.message === "string") {
        return body.message;
    }
    return statusText || "The request could not be completed.";
}

async function postJson(url, payload) {
    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
    const text = await response.text();
    let body;

    if (text) {
        try {
            body = JSON.parse(text);
        } catch {
            if (response.ok) {
                throw new Error("The server returned an invalid response.");
            }
        }
    }

    if (!response.ok) {
        throw new Error(getErrorMessage(body, response.statusText));
    }
    return body;
}

export function getAuthToken() {
    return sessionStorage.getItem(TOKEN_STORAGE_KEY);
}

export function clearAuthToken() {
    sessionStorage.removeItem(TOKEN_STORAGE_KEY);
}

export function mountAuthForm(container, options = {}) {
    if (!(container instanceof Element)) {
        throw new TypeError("An element is required to mount the authentication form.");
    }

    const {
        mode = "login",
        closable = false,
        onAuthenticated = () => {}
    } = options;
    if (!["login", "register", "both"].includes(mode)) {
        throw new TypeError("Authentication mode must be login, register, or both.");
    }

    container.innerHTML = authMarkup;
    const loginForm = container.querySelector('[data-auth-form="login"]');
    const registerForm = container.querySelector('[data-auth-form="register"]');
    const title = container.querySelector("#auth-title");
    const description = container.querySelector("[data-auth-description]");
    const switchLabel = container.querySelector("[data-auth-switch-label]");
    const switchButton = container.querySelector("[data-auth-switch]");
    const status = container.querySelector("[data-auth-status]");
    const closeButton = container.querySelector(".auth-close");
    title.id = `auth-title-${nextAuthFormId++}`;
    container.querySelector(".auth-panel").setAttribute("aria-labelledby", title.id);
    closeButton.hidden = !closable;
    let currentMode = mode === "register" ? "register" : "login";

    if (mode === "both") {
        switchButton.addEventListener("click", () => {
            setMode(currentMode === "login" ? "register" : "login");
        });
    } else {
        container.querySelector(".auth-switch").hidden = true;
    }

    function setMode(nextMode) {
        currentMode = nextMode;
        const isLogin = currentMode === "login";
        loginForm.hidden = !isLogin;
        registerForm.hidden = isLogin;
        title.textContent = isLogin ? "Welcome back" : "Create your account";
        description.textContent = isLogin
            ? "Sign in to continue."
            : "Join us in making more thoughtful choices.";
        switchLabel.textContent = isLogin ? "New here?" : "Already have an account?";
        switchButton.textContent = isLogin ? "Register" : "Sign in";
        status.textContent = "";
        delete status.dataset.state;
    }

    function showStatus(message, state) {
        status.textContent = message;
        status.dataset.state = state;
    }

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const submitButton = loginForm.querySelector('[type="submit"]');
        submitButton.disabled = true;
        showStatus("Signing in…", "pending");

        try {
            const formData = new FormData(loginForm);
            const response = await postJson(`${AUTH_API}/login`, {
                email: formData.get("email"),
                password: formData.get("password")
            });
            if (typeof response?.token !== "string" || !response.token) {
                throw new Error("The login response did not contain an access token.");
            }

            sessionStorage.setItem(TOKEN_STORAGE_KEY, response.token);
            showStatus("Signed in. Loading your account…", "success");
            container.dispatchEvent(new CustomEvent("auth:authenticated", {
                bubbles: true,
                detail: { token: response.token }
            }));
            await onAuthenticated(response.token);
        } catch (error) {
            showStatus(error.message || "Unable to sign in. Please try again.", "error");
        } finally {
            submitButton.disabled = false;
        }
    });

    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const submitButton = registerForm.querySelector('[type="submit"]');
        const formData = new FormData(registerForm);
        const password = formData.get("password");
        if (password !== formData.get("repeatPassword")) {
            showStatus("Passwords do not match.", "error");
            return;
        }

        submitButton.disabled = true;
        showStatus("Creating your account…", "pending");
        try {
            await postJson(`${AUTH_API}/registration`, {
                email: formData.get("email"),
                password,
                repeatPassword: formData.get("repeatPassword"),
                firstName: formData.get("firstName"),
                lastName: formData.get("lastName")
            });
            loginForm.elements.email.value = formData.get("email");
            setMode("login");
            showStatus("Account created. Sign in to continue.", "success");
        } catch (error) {
            showStatus(error.message || "Unable to create your account. Please try again.", "error");
        } finally {
            submitButton.disabled = false;
        }
    });

    closeButton.addEventListener("click", () => {
        container.dispatchEvent(new CustomEvent("auth:close", { bubbles: true }));
    });

    setMode(currentMode);
    return {
        setMode,
        destroy() {
            container.replaceChildren();
        }
    };
}

export function openAuthDialog(options = {}) {
    const dialog = document.createElement("dialog");
    dialog.className = "auth-dialog";
    document.body.append(dialog);
    const { onAuthenticated = () => {}, ...authOptions } = options;
    let component;
    const close = () => {
        dialog.close();
        component.destroy();
        dialog.remove();
    };
    component = mountAuthForm(dialog, {
        ...authOptions,
        closable: true,
        onAuthenticated: async (token) => {
            await onAuthenticated(token);
            close();
        }
    });

    dialog.addEventListener("auth:close", close);
    dialog.addEventListener("click", (event) => {
        if (event.target === dialog) {
            close();
        }
    });
    dialog.addEventListener("close", () => {
        component.destroy();
        dialog.remove();
    }, { once: true });
    dialog.showModal();
    return { dialog, close };
}
