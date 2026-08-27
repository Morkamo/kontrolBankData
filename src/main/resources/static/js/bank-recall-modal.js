const modal = document.querySelector('.modal');
const openModalButton = document.querySelector('[data-open-modal]');
const cancelModalButton = document.querySelector('[data-close-modal]');
const journalForm = document.querySelector('.modal-form');
const deleteRecordForms = document.querySelectorAll('.delete-record-form');
const numericInputs = document.querySelectorAll('input[data-numeric]');
const nameInputs = document.querySelectorAll('input[data-person-name]');
const amountInputs = document.querySelectorAll('input[data-amount]');
const periodFieldsets = document.querySelectorAll('.period-fieldset, .period-search-fieldset');

function closeModal() {
    modal?.classList.remove('modal-open');
}

openModalButton?.addEventListener('click', () => modal?.classList.add('modal-open'));
cancelModalButton?.addEventListener('click', closeModal);

modal?.addEventListener('click', (event) => {
    if (event.target === modal) {
        closeModal();
    }
});

deleteRecordForms.forEach((form) => {
    form.addEventListener('submit', (event) => {
        if (!window.confirm(form.dataset.confirmMessage || 'Удалить запись?')) {
            event.preventDefault();
        }
    });
});

numericInputs.forEach((input) => {
    input.addEventListener('input', () => {
        input.value = input.value.replace(/\D/g, '').slice(0, input.maxLength);
    });
});

nameInputs.forEach((input) => {
    input.addEventListener('input', () => {
        input.value = input.value.replace(/[^А-Яа-яЁё\s-]/g, '');
    });
});

amountInputs.forEach((input) => {
    input.addEventListener('input', () => {
        input.value = input.value
            .replace(/[^0-9,.]/g, '')
            .replace(',', '.')
            .replace(/^(\d*\.?\d{0,2}).*$/, '$1');
    });
});

function validatePeriodDates(fieldset) {
    const start = fieldset.querySelector('input[name="periodStart"]');
    const end = fieldset.querySelector('input[name="periodEnd"]');

    if (!start || !end) {
        return true;
    }

    end.min = start.value || '';
    const invalid = start.value && end.value && start.value > end.value;
    end.setCustomValidity(invalid ? 'Дата ПО не может быть раньше даты С' : '');
    return !invalid;
}

periodFieldsets.forEach((fieldset) => {
    fieldset.querySelectorAll('input[type="date"]').forEach((input) => {
        input.addEventListener('input', () => validatePeriodDates(fieldset));
        input.addEventListener('change', () => validatePeriodDates(fieldset));
    });

    validatePeriodDates(fieldset);
});

journalForm?.addEventListener('submit', (event) => {
    const periodFieldset = journalForm.querySelector('.period-fieldset');

    if (periodFieldset && !validatePeriodDates(periodFieldset)) {
        event.preventDefault();
        periodFieldset.querySelector('input[name="periodEnd"]').reportValidity();
    }
});
