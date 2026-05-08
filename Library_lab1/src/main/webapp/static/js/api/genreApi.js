export const GenreApi = {
	async getAll() {
        const response = await fetch(`/api/genres`);
        if (!response.ok) throw new Error(`Failed to fetch genres: ${response.status}`);
        const data = await response.json();
        return data;
    },
    async getById(id) {
        const response = await fetch(`/api/genres?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch genres by id: ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data[0] : data;
    }
};