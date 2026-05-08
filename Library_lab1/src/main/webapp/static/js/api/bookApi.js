export const BookApi = {
	async getAll(params) {
        const qs = params ? `?${params.toString()}` : '';
        const response = await fetch(`/api/books${qs}`);
        if (!response.ok) throw new Error(`Failed to fetch books: ${response.status}`);
            return await response.json();
    },
    async getById(id) {
        const response = await fetch(`/api/books?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch books by id: ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data[0] : data;
    }
};