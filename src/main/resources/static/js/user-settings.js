const THEME_KEY = 'kontrolBankData.themeDark';
const ACCESSIBILITY_KEY = 'kontrolBankData.accessibilityLarge';
const ACCESSIBILITY_SCALE_KEY = 'kontrolBankData.accessibilityScale';
const DEFAULT_ACCESSIBILITY_SCALE = 120;
const MIN_ACCESSIBILITY_SCALE = 100;
const MAX_ACCESSIBILITY_SCALE = 150;

function storedSetting(key) {
    try {
        return localStorage.getItem(key) === 'true';
    } catch (_) {
        return false;
    }
}

function saveSetting(key, enabled) {
    try {
        localStorage.setItem(key, String(enabled));
    } catch (_) {
        // Интерфейс будет работать даже если localStorage запрещен браузером.
    }
}

function applySetting(className, enabled) {
    document.body.classList.toggle(className, enabled);
}

function storedScale() {
    try {
        const value = Number(localStorage.getItem(ACCESSIBILITY_SCALE_KEY));
        if (Number.isFinite(value)) {
            return Math.min(MAX_ACCESSIBILITY_SCALE, Math.max(MIN_ACCESSIBILITY_SCALE, value));
        }
    } catch (_) {
        // Интерфейс будет работать даже если localStorage запрещен браузером.
    }
    return DEFAULT_ACCESSIBILITY_SCALE;
}

function saveScale(scale) {
    try {
        localStorage.setItem(ACCESSIBILITY_SCALE_KEY, String(scale));
    } catch (_) {
        // Интерфейс будет работать даже если localStorage запрещен браузером.
    }
}

function applyAccessibility(enabled, scale) {
    applySetting('accessibility-large', enabled === true);
    document.body.style.setProperty('--accessibility-scale', String(scale / 100));
}

const themeEnabled = storedSetting(THEME_KEY);
const accessibilityEnabled = storedSetting(ACCESSIBILITY_KEY);
const accessibilityScale = storedScale();
applySetting('theme-dark', themeEnabled);
applyAccessibility(accessibilityEnabled, accessibilityScale);

const settingsButton = document.querySelector('[data-settings-toggle]');
const settingsDropdown = document.querySelector('[data-settings-dropdown]');
const themeToggle = document.querySelector('[data-theme-toggle]');
const accessibilityToggle = document.querySelector('[data-accessibility-toggle]');
const accessibilityScaleControl = document.querySelector('[data-accessibility-scale-control]');
const accessibilityScaleInput = document.querySelector('[data-accessibility-scale]');
const accessibilityScaleOutput = document.querySelector('[data-accessibility-scale-output]');

function syncScaleControl(enabled, scale) {
    if (accessibilityScaleControl) {
        accessibilityScaleControl.hidden = !enabled;
    }
    if (accessibilityScaleInput) {
        accessibilityScaleInput.disabled = !enabled;
        accessibilityScaleInput.value = String(scale);
    }
    if (accessibilityScaleOutput) {
        accessibilityScaleOutput.value = `${scale}%`;
        accessibilityScaleOutput.textContent = `${scale}%`;
    }
}

syncScaleControl(accessibilityEnabled, accessibilityScale);

if (themeToggle) {
    themeToggle.checked = themeEnabled;
    themeToggle.addEventListener('change', () => {
        applySetting('theme-dark', themeToggle.checked);
        saveSetting(THEME_KEY, themeToggle.checked);
    });
}

if (accessibilityToggle) {
    accessibilityToggle.checked = accessibilityEnabled;
    accessibilityToggle.addEventListener('change', () => {
        const scale = storedScale();
        applyAccessibility(accessibilityToggle.checked, scale);
        syncScaleControl(accessibilityToggle.checked, scale);
        saveSetting(ACCESSIBILITY_KEY, accessibilityToggle.checked);
    });
}

accessibilityScaleInput?.addEventListener('input', () => {
    const scale = Number(accessibilityScaleInput.value);
    applyAccessibility(accessibilityToggle?.checked, scale);
    syncScaleControl(accessibilityToggle?.checked, scale);
    saveScale(scale);
});

settingsButton?.addEventListener('click', () => {
    settingsDropdown.hidden = !settingsDropdown.hidden;
    settingsButton.setAttribute('aria-expanded', String(!settingsDropdown.hidden));
});

document.addEventListener('click', (event) => {
    if (settingsDropdown && !settingsDropdown.hidden && !event.target.closest('.settings-menu')) {
        settingsDropdown.hidden = true;
        settingsButton?.setAttribute('aria-expanded', 'false');
    }
});

document.querySelectorAll('.logout-form').forEach((form) => {
    form.addEventListener('submit', (event) => {
        if (!window.confirm('Вы действительно хотите выйти?')) {
            event.preventDefault();
        }
    });
});
