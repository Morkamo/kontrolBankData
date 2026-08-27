const bankRecallModal = document.getElementById('bankRecallModal');
const openBankRecallModal = document.getElementById('openBankRecallModal');
const cancelBankRecallModal = document.getElementById('cancelBankRecallModal');
const deleteRecordForms = document.querySelectorAll('.delete-record-form');
const numericInputs = document.querySelectorAll('input[name="pensionCaseNumber"], input[name="packageNumber"]');
const pensionerNameInput = document.querySelector('input[name="pensionerName"]');
const bankRecallForm = document.getElementById('bankRecallForm');
const periodFieldsets = document.querySelectorAll('.period-search-fieldset, .period-fieldset');

function closeModal() {
    bankRecallModal.classList.remove('modal-open');
}

if (openBankRecallModal) {
    openBankRecallModal.addEventListener('click', () => {
        bankRecallModal.classList.add('modal-open');
    });
}

if (cancelBankRecallModal) {
    cancelBankRecallModal.addEventListener('click', closeModal);
}

if (bankRecallModal) {
    bankRecallModal.addEventListener('click', (event) => {
        if (event.target === bankRecallModal) {
            closeModal();
        }
    });
}

deleteRecordForms.forEach((form) => {
    form.addEventListener('submit', (event) => {
        const message = form.dataset.confirmMessage || 'Удалить запись?';

        if (!window.confirm(message)) {
            event.preventDefault();
        }
    });
});

numericInputs.forEach((input) => {
    input.addEventListener('input', () => {
        input.value = input.value.replace(/\D/g, '').slice(0, input.maxLength);
    });
});

if (pensionerNameInput) {
    pensionerNameInput.addEventListener('input', () => {
        pensionerNameInput.value = pensionerNameInput.value.replace(/[^А-Яа-яЁё\s-]/g, '');
    });
}

function formatPeriodDate(value) {
    if (!value) {
        return '';
    }

    const [year, month, day] = value.split('-');
    return `${day}.${month}.${year}`;
}

function updatePeriodValue(fieldset) {
    const periodInput = fieldset.querySelector('input[name="period"]');
    const periodStartInput = fieldset.querySelector('input[name="periodStart"]');
    const periodEndInput = fieldset.querySelector('input[name="periodEnd"]');

    if (!periodInput || !periodStartInput || !periodEndInput) {
        return;
    }

    periodInput.value = `с ${formatPeriodDate(periodStartInput.value)} по ${formatPeriodDate(periodEndInput.value)}`;
}

function validatePeriodDates(fieldset) {
    const periodStartInput = fieldset.querySelector('input[name="periodStart"]');
    const periodEndInput = fieldset.querySelector('input[name="periodEnd"]');

    if (!periodStartInput || !periodEndInput) {
        return true;
    }

    periodEndInput.min = periodStartInput.value || '';

    const hasInvalidRange = periodStartInput.value && periodEndInput.value
        && periodStartInput.value > periodEndInput.value;

    periodEndInput.setCustomValidity(hasInvalidRange ? 'Дата ПО не может быть раньше даты С' : '');
    return !hasInvalidRange;
}

periodFieldsets.forEach((fieldset) => {
    const periodStartInput = fieldset.querySelector('input[name="periodStart"]');
    const periodEndInput = fieldset.querySelector('input[name="periodEnd"]');

    [periodStartInput, periodEndInput].forEach((input) => {
        if (!input) {
            return;
        }

        input.addEventListener('input', () => {
            validatePeriodDates(fieldset);
            updatePeriodValue(fieldset);
        });

        input.addEventListener('change', () => {
            validatePeriodDates(fieldset);
            updatePeriodValue(fieldset);
        });
    });

    validatePeriodDates(fieldset);
});

if (bankRecallForm) {
    bankRecallForm.addEventListener('submit', (event) => {
        const periodFieldset = bankRecallForm.querySelector('.period-fieldset');

        updatePeriodValue(periodFieldset);

        if (!validatePeriodDates(periodFieldset)) {
            event.preventDefault();
            periodFieldset.querySelector('input[name="periodEnd"]').reportValidity();
        }
    });
}
