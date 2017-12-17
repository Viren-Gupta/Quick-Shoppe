
	  var config = {
	    apiKey: "AIzaSyDGvYXRFEIcnQNDsNTCaSzv-H74rmx4uns",
	    authDomain: "quick-shope.firebaseapp.com",
	    databaseURL: "https://quick-shope.firebaseio.com",
	    projectId: "quick-shope",
	    storageBucket: "quick-shope.appspot.com",
	    messagingSenderId: "901197315899"
	  };
	  firebase.initializeApp(config);

	document.getElementById('loginFormId').addEventListener('submit',submitForm);


	function removeClasses(r_id,c){
		document.getElementById(r_id).classList.remove(c);
	}

	function submitForm(e){
		load=" fa fa-circle-o-notch fa-spin";
		document.getElementById('loadingCircle').className+=load;
		/*document.getElementById('submitButton').style.backgroundColor='#6495ED';	*/
		e.preventDefault();
		var username = document.getElementById('usernameInput').value;
		var password=document.getElementById('passwordInput').value;
		verify(username,password)
		.then(function(){
			removeClasses('loadingCircle',"fa");
			removeClasses('loadingCircle',"fa-circle-o-notch");
			removeClasses('loadingCircle',"fa-spin");
			
			sessionStorage.shopID=username;
			sessionStorage.loginStatus="in";
			window.location="shopUI.html";
		})
		.catch(function(){
			removeClasses('loadingCircle',"fa");
			removeClasses('loadingCircle',"fa-circle-o-notch");
			removeClasses('loadingCircle',"fa-spin");
			document.getElementById('submitButton').setAttribute("data-toggle","modal");
			$("#myModal").modal("show");
		});

		
				
		
	}

	function verify(username,password){
		return new Promise(function (resolve, reject){
			var ref = firebase.database().ref(username);
			ref.once('value').then(function(snap){	
				snap.forEach(function (childSnapshot){
					if(childSnapshot.key.localeCompare('password')==0){
							if(childSnapshot.val().localeCompare(password)==0){
									return resolve();
							}
							return reject();			
					}
				});
				return reject();
			});
			
		});		
		
	}