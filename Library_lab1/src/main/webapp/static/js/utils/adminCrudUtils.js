const toCamel = (str) => str.replace(/-([a-z])/g, (g) => g[1].toUpperCase());

export const getFormData = (formEl) => {
    if (!formEl) return {};
    const formData = new FormData(formEl);
    const payload = {};

    for (let [key, value] of formData.entries()) {
        const cleanKey = toCamel(key);
        payload[cleanKey] = typeof value === 'string' ? value.trim() : value;
    }
    return payload;
};

export const fillFormWithData = (formEl, data) => {
    if (!formEl || !data) return;

    Object.entries(data).forEach(([key, value]) => {
        const hyphenKey = key.replace(/[A-Z]/g, m => "-" + m.toLowerCase());
        const input = formEl.elements[key] || formEl.elements[hyphenKey];
        if (input) {
            input.value = value ?? "";
        }
    });
};