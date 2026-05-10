async function updateHeader() {
		const response = await fetch('/api/auth/status');
		const auth = await response.json();
		const authContainer = document.getElementById('auth-container');
		if(auth.authenticated) {
			let roleTitle ='';
			let roleRef = '';
			if(auth.role === 'READER') {
				roleTitle = 'Orders';
				roleRef='#' //temporary
			}
			else{
				roleTitle = 'Management panel';
				roleRef="/management" //temporary
			}
			// # - are temporary
			authContainer.innerHTML = `
				<div class="profile-menu">
                    <p>${auth.firstName} ${auth.lastName}</p>
                    <div class="drop-down-menu">
                        <div><a href="#">Profile</a></div
                        <hr>
                        <div><a href=${roleRef}>${roleTitle}</a></div>
                        <hr>
                        <div><a href="#">Log out</a></div>
                    </div>
                </div>
			`;
		}
		else{
			authContainer.innerHTML = `
				<a href="/login" class="sign-in-btn">Sign in</a>
            `;
		}
	};

document.addEventListener('DOMContentLoaded', updateHeader);