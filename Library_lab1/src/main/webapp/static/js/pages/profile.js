import { UserApi } from '../api/userApi.js';
import { fillUserInfo } from '../utils/profileFillInfo.js';

document.addEventListener('DOMContentLoaded', async () => {
	const profilePagePath = document.getElementById('profile-personal-page');

	async function init() {

		const statusResponse = await fetch('/api/auth/status');
		const auth = await statusResponse.json();
		if(!auth.id){
			console.log("No id was found.");
            return;
        }
			const user = await UserApi.getById(auth.id);
			if (!user || user.error) {
                console.log("Something went wrong.");
            	return;
            }
			console.log(user);
			fillUserInfo(user);

		if(auth.role === 'LIBRARIAN'){
			const optionsList = document.getElementById('options-list');
			optionsList.innerHTML += '<li class="profile-nav-item"><a href="/management">Management</a></li>'
		}

	}

	init();

})