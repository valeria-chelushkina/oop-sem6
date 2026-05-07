/**
 * Creates HTML row for checkbox
 */
function createFilterCheckbox(name, value) {
    return `
        <label class="form-control">
            <input type="checkbox" name="${name}" value="${value}" />
            <span>${value}</span>
        </label>
    `;
}

/**
 * Renders received filters in corresponding containers
 */
export function renderFilterGroups(filters) {
    const genreContainer = document.querySelector('#genre-filters');
    const languageContainer = document.querySelector('#language-filters');

    if (genreContainer && filters.genres) {
        genreContainer.innerHTML = '<legend>Genres & tags</legend>' +
            filters.genres.map(g => createFilterCheckbox('genre', g)).join('');
    }

    if (languageContainer && filters.languages) {
        languageContainer.innerHTML = '<legend>Languages</legend>' +
            filters.languages.map(l => createFilterCheckbox('language', l)).join('');
    }
}