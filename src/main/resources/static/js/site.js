import { mountProducts } from "./products.js";
import {
    clearAuthToken,
    getAuthToken,
    openAuthDialog
} from "./auth.js";

const contentContainer = document.querySelector("#content-container");
const siteNav = document.querySelector(".site-nav");

function renderSignedOutNavigation() {
    const signInLink = document.createElement("a");
    signInLink.className = "nav-action";
    signInLink.href = "#sign-in";
    signInLink.dataset.authOpen = "";
    signInLink.textContent = "Sign in";
    signInLink.addEventListener("click", openSignInDialog);
    siteNav.querySelector(".nav-action")?.replaceWith(signInLink);
}

function renderSignedInNavigation(user) {
    const profileLink = document.createElement("a");
    profileLink.className = "nav-action";
    profileLink.href = "#profile";
    profileLink.textContent = `Hi, ${user.firstName}`;
    profileLink.title = `Signed in as ${user.email}`;
    siteNav.querySelector(".nav-action")?.replaceWith(profileLink);
}

function renderMessage(message, isError = false) {
    contentContainer.replaceChildren();
    const paragraph = document.createElement("p");
    paragraph.className = "content-message";
    paragraph.textContent = message;
    if (isError) {
        paragraph.setAttribute("role", "alert");
    }
    contentContainer.append(paragraph);
}

function renderAccount(user) {
    const card = document.createElement("section");
    card.className = "account-card";
    const details = document.createElement("div");
    const heading = document.createElement("h2");
    heading.textContent = `Welcome back, ${user.firstName}`;
    const email = document.createElement("p");
    email.textContent = `Signed in as ${user.email}`;
    const signOut = document.createElement("button");
    signOut.className = "text-button";
    signOut.type = "button";
    signOut.textContent = "Sign out";
    signOut.addEventListener("click", () => {
        clearAuthToken();
        renderSignedOutNavigation();
        productView.refresh();
    });
    details.append(heading, email);
    card.append(details, signOut);
    contentContainer.prepend(card);
    renderSignedInNavigation(user);
}

async function loadAccount(token) {
    const response = await fetch("/api/users", {
        headers: { Authorization: `Bearer ${token}` }
    });
    if (!response.ok) {
        const body = await response.json().catch(() => null);
        const message = typeof body?.errors === "string"
            ? body.errors
            : `Unable to load your account (${response.status}).`;
        throw new Error(message);
    }
    renderAccount(await response.json());
}

function openSignInDialog(event) {
    event.preventDefault();
    openAuthDialog({
        mode: "both",
        onAuthenticated: async (token) => {
            try {
                await loadAccount(token);
            } catch (error) {
                renderMessage(
                    `${error.message} Your sign-in is saved; you can try again without signing in again.`,
                    true
                );
                throw error;
            }
        }
    });
}

renderSignedOutNavigation();
const productView = mountProducts(contentContainer);

const existingToken = getAuthToken();
if (existingToken) {
    loadAccount(existingToken).catch(() => {
        clearAuthToken();
        renderSignedOutNavigation();
    });
}
