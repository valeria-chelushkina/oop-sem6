export const UrlService = {
    /**
     * Forms URL parameters.
     * Both arguments are not necessary.
     */
    getParamsFromForm(searchBar = null, filterForm = null) {
        const params = new URLSearchParams();

        if (searchBar && searchBar.value) {
            const q = searchBar.value.trim();
            if (q) params.set('query', q);
        } else {
            const currentParams = new URLSearchParams(window.location.search);
            const existingQuery = currentParams.get('query');
            if (existingQuery) params.set('query', existingQuery);
        }

        if (filterForm) {
            filterForm.querySelectorAll('input[name="genre"]:checked').forEach(el => params.append('genre', el.value));
            filterForm.querySelectorAll('input[name="language"]:checked').forEach(el => params.append('language', el.value));
        } else {
            const currentParams = new URLSearchParams(window.location.search);
            currentParams.getAll('genre').forEach(v => params.append('genre', v));
            currentParams.getAll('language').forEach(v => params.append('language', v));
        }

        return params;
    },

    applyUrlToForm(searchBar, filterForm) {
        const params = new URLSearchParams(window.location.search);

        if (searchBar) {
            searchBar.value = params.get('query') || '';
        }

        if (filterForm) {
            const selectedGenres = params.getAll('genre');
            const selectedLanguages = params.getAll('language');

            filterForm.querySelectorAll('input[name="genre"]').forEach(el => {
                el.checked = selectedGenres.includes(el.value);
            });
            filterForm.querySelectorAll('input[name="language"]').forEach(el => {
                el.checked = selectedLanguages.includes(el.value);
            });
        }
    },

    navigateWithParams(params) {
        const qs = params.toString();
        const path = '/';
        window.location.assign(qs ? `${path}?${qs}` : path);
    }
};