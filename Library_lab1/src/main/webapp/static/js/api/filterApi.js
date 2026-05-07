export const FilterApi = {
    async getFilters() {
        const response = await fetch('/api/filters');
        if (!response.ok) {
            throw new Error(`Failed to load filters: ${response.status}`);
        }
        return await response.json();
    }
};