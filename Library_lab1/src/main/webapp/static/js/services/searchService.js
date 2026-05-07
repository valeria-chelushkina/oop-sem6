export const SearchService = {
    handleSearch(searchBar) {
        const query = searchBar.value.trim();
        const path = '/';
        window.location.assign(query ? `${path}?query=${query}` : path);
    },

    getParamsFromUrl() {
        return new URLSearchParams(window.location.search);
    }
};