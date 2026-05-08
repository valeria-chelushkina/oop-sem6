const API_URL = '/api/book-items';

export const BookItemApi = {
	async getByBookId(bookId) {
        const response = await fetch(`${API_URL}?bookId=${bookId}`);
        if (!response.ok) throw new Error(`Failed to fetch book items by book id: ${response.status}`);
        const data = await response.json();
        return data;
    },
    async getById(id) {
        const response = await fetch(`${API_URL}?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch book items by id: ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data[0] : data;
        },
    async getAll() {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error(`Failed to fetch book items: ${response.status}`);
        const data = await response.json();
        return data;
    },
	async create(payload) {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
	            'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });
        if(!response.ok){
            throw new Error(`Failed to create book item: ${response.status}`);
        };
        return await response.json();
    },
    async update(payload) {
		const response = await fetch(API_URL, {
            method: 'PUT',
            headers: {
	            'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });
        if(!response.ok){
            throw new Error(`Failed to update book item: ${response.status}`);
        };
        return await response.json();
    },
    async delete(id) {
            const response = await fetch(`${API_URL}?id=${id}`, {
                method: 'DELETE'
            });
            return response.ok;
        }
};