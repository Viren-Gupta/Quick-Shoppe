			  //configuration of firebase db 
			  var config = {
			    apiKey: "AIzaSyDGvYXRFEIcnQNDsNTCaSzv-H74rmx4uns",
			    authDomain: "quick-shope.firebaseapp.com",
			    databaseURL: "https://quick-shope.firebaseio.com",
			    projectId: "quick-shope",
			    storageBucket: "quick-shope.appspot.com",
			    messagingSenderId: "901197315899"
			  };

			  firebase.initializeApp(config);


			nameOfShop='';
			orderList='';
			totalBill=0;

			name_temp='';
			quantity_temp=0;
			price_temp=0;

			document.getElementById('placeOrder').addEventListener('submit',place);
			
			//prevent UI page to open without authentication
			if(sessionStorage.loginStatus==null || sessionStorage.loginStatus.localeCompare("out")==0){
				window.location="./shopLogin.html";
			}
			//logout
			function logout(){
				sessionStorage.loginStatus="out";
				sessionStorage.shopID=null;
				window.location="./shopLogin.html";
			}

			// loading circle on page loading
			function createLoader(){
				console.log('in');
				var d=document.createElement("div");
				d.style.zIndex=2;
				d.style.position='absolute';
				d.style.top='50%';
				d.style.left='50%';
				d.id="loader";
				d.className="loader";
				document.body.style.opacity=0.5;
				document.body.appendChild(d);
			}

			// called first when page is loaded
			function loading(){
					createLoader();
					id=sessionStorage.shopID;
					getName(id)
					.then(function(){
						document.getElementById('ShopName').innerHTML=nameOfShop;
					})
					.catch(function(){
						;
					});
					 
					//show the pending orders
					fillTable('portalTable',id)
					.then(function(){
						document.body.style.opacity=1;
						document.getElementById("loader").remove();
					})
					.catch(function(){
						;
					});
					


			}

			function updateDB(name,quantity,price,barcode){

				id=sessionStorage.shopID;
				return new Promise(function (resolve, reject){
						
					if(sessionStorage.editStatus.localeCompare("add")==0){
						var addr=id.concat("/items");
						var ref=firebase.database().ref(addr);
						ref.push({
							barCode:barcode,
							name:name,
							price:(float)(price),
							quantity:(float)(quantity)
						});
						return resolve();
					}else if(sessionStorage.editStatus.localeCompare("remove")==0){

						var addr=id.concat("/items/");
						addr=addr.concat(barcode.toString());
						
						var ref=firebase.database().ref(addr);
						ref.remove();
						return resolve();

					}else if(sessionStorage.editStatus.localeCompare("edit")==0){
						var addr=id.concat("/items/");
						addr=addr.concat(barcode.toString());
						var ref = firebase.database().ref(addr.concat("/name"));
						ref.set(name);

						ref = firebase.database().ref(addr.concat("/price"));
						ref.set(price);

						ref = firebase.database().ref(addr.concat("/quantity"));
						ref.set(quantity);
						console.log('editttt');
							
						return resolve();
					}
								
				});
			}

			//updates the db after changes are made about the items present in the shop

			function editDB(){
				item_name_form = document.getElementById("item_name").value;
				item_price_form = document.getElementById("price").value;
				item_quantity_form = document.getElementById("quantity").value;
				item_barcode_form = document.getElementById("barcode").value;

				sessionStorage.editStatus="edit";
				updateDB(item_name_form,item_quantity_form,item_price_form,item_barcode_form)
					.then(function(){
						console.log("updateDB");
					})
					.catch(function(){

					})

			}

			function deleteItem(){
				
				item_barcode_form = document.getElementById("barcode").value;
				sessionStorage.editStatus="remove";
				updateDB("","","",item_barcode_form)
					.then(function(){
						console.log("updateDB");
					})
					.catch(function(){

					})

			}

			function getValues(id,barcode){
				return new Promise(function (resolve, reject){
					var addr=id.concat("/items/");
					addr=addr.concat(barcode);
					var ref = firebase.database().ref(addr);
					ref.once('value').then(function(snap){	
						snap.forEach(function (subChild){
							if(subChild.key.localeCompare('price')==0){
									price_temp=subChild.val();
							}
							if(subChild.key.localeCompare('name')==0){
									name_temp=subChild.val().toString();
							}
							if(subChild.key.localeCompare('quantity')==0){
									quantity_temp=subChild.val();
							}
						});
						
						return resolve();
					});
				});	
			}

			
			function edit(e){
				var barcode=(e.target.id).toString();
				var id=sessionStorage.shopID;
				getValues(id,barcode)
					.then(function(){
						document.getElementById("item_name").value=name_temp;
						document.getElementById("quantity").value=quantity_temp;
						document.getElementById("price").value=price_temp;
						document.getElementById("barcode").value=barcode;
						//document.getElementById('editButton').setAttribute("data-toggle","modal");
						$("#myModal_edit").modal("show");

					})
					.catch(function(){
						;
					});
					
				
			}

			//removes the entry from db once order is completed

			function finishTransaction(e){

				console.log('yes');
				id=sessionStorage.shopID;
				addr=id.concat("/customers/");
				checkoutID=(e.target.id).split(" ")[1];
				addr=addr.concat(checkoutID.toString());
				addr=addr.concat("/status");
				var ref = firebase.database().ref(addr);

				
				bootbox.confirm({
				    message: "Mark the order as done?",
				    buttons: {
				        confirm: {
				            label: 'Done',
				            className: 'btn-success'
				        },
				        cancel: {
				            label: 'Cancel',
				            className: 'btn-danger'
				        }
				    },
				    callback: function (result) {
				        if(result==true){

							ref.set("dead");
							location.reload()
						}
						else{
							
						}
				    }
				});

			}

			// creates the table for showing list of items for a customer

			function GenerateTable(arr) {
			   
			    var items = new Array();
			    items.push(["Barcode","Name", "Price", "Quantity",]);
			    
			    for(var i=0;i<arr.length;i++){
			    	items.push(arr[i]);
			    }

			    var table = document.createElement("TABLE");
			   	table.className="table";
			    
			    var columnCount = items[0].length;
			 
			    var row = table.insertRow(-1);
			    for (var i = 0; i < columnCount; i++) {
			        var headerCell = document.createElement("TH");
			        headerCell.innerHTML = items[0][i];
			        row.appendChild(headerCell);
			    }


			    for (var i = 1; i < items.length; i++) {
			        row = table.insertRow(-1);
			        for (var j = 0; j < columnCount; j++) {

			            var cell = row.insertCell(-1);
			            cell.innerHTML = items[i][j];
			        }

			        totalBill+=items[i][2]*items[i][3];
			    }
			 
			 	return table;
			}

			//fills one row corresponding to a customer
			function fillRow(tableName,c1,c2,c3){
					var table=document.getElementById(tableName);
					var numRows=table.rows.length;
					var row=table.insertRow(numRows);
					var cell1 = row.insertCell(0);
			    	var cell2 = row.insertCell(1);
			    	var cell3=	row.insertCell(2);
			    	var cell4=	row.insertCell(3);
			    	var cell5=	row.insertCell(4);
			    	
			    	
			    	cell1.innerHTML = c1;
			    	cell1.style.fontWeight='bold'
			    	var acc=document.createElement('BUTTON');
			    	var div = document.createElement("div");
			    	div.className="panel";
			    	acc.className="accordion";
			    	//acc.className+=" btn btn-primary";

			    	var tab=GenerateTable(c2);

			    	acc.appendChild(tab);
			    	var i;
			     		acc.onclick = function(){
				        this.classList.toggle("active");
				        var panel = this.childNodes[0];
				        if (panel.style.display === "block") {
				            panel.style.display = "none";
				        } else {
				            panel.style.display = "block";
				        }
				    }
			    	cell3.appendChild(acc);
			    	var done=document.createElement('BUTTON');
			    	done.className="btn btn-primary btn-sm glyphicon glyphicon-ok";

			    	/*var sp = document.createElement('span');
			    	sp.className="";
			    	*/term='clear '.concat(c1);
			    	//sp.id=term;
			    	done.id=term;
			    	done.addEventListener('click',finishTransaction);

			    	//done.appendChild(sp);
			    	cell5.appendChild(done);

					cell2.innerHTML = c3;
					cell2.style.fontWeight='bold'
					cell4.innerHTML=totalBill;
					cell4.style.fontWeight='bold'
					totalBill=0;
			}

			//refers db and get all the pending orders
			function fillTable(tableName,id){

				return new Promise(function (resolve, reject){
					var addr=id.concat("/customers");
					var ref = firebase.database().ref(addr);
					ref.once('value').then(function(snap){	
						snap.forEach(function (childSnapshot){
							identity=childSnapshot.key;
							var purchases=[];
							time='';
							check=false;
							childSnapshot.forEach(function(subChild){
								if(subChild.key.localeCompare('time')==0){
									time=subChild.val().toString();
								}

								if(subChild.key.localeCompare('purchases')==0){
									subChild.forEach(function(subsubChild){
										item=[];
										subsubChild.forEach(function(sssChild){
											item.push(sssChild.val());
										});	
										purchases.push(item);
									});
								}
								if(subChild.key.localeCompare('status')==0){
									if(subChild.val().localeCompare('active')==0){
										check=true;
										//fillRow(identity,purchases,time);
									}
								}
									
							});

							if(check==true)
								fillRow(tableName,identity,purchases,time);
							
						});
						
						return resolve();
					});
					
				});	
			}

			//fills the table which shows the items in shop

			function fillRow_change(tableName,c1,c2,c3,barcode){
					var table=document.getElementById(tableName);
					var numRows=table.rows.length;
					var row=table.insertRow(numRows);
					var cell1 = row.insertCell(0);
			    	var cell2 = row.insertCell(1);
			    	var cell3=	row.insertCell(2);
			    	var cell4=	row.insertCell(3);
			    	
			    	
			    	cell1.innerHTML = c1;
			    	cell2.innerHTML = c2;
			    	cell3.innerHTML = c3;

			    	var editButton = document.createElement('BUTTON');
			    	editButton.id=barcode.toString();
			    	editButton.className="glyphicon glyphicon-pencil";
			    	cell4.appendChild(editButton);
			    	editButton.addEventListener("click", edit);
			}

			//gets the items present in shop and displays them

			function fillTable_change(tableName,id){

				return new Promise(function (resolve, reject){
					var addr=id.concat("/items");
					var ref = firebase.database().ref(addr);
					ref.once('value').then(function(snap){	
						snap.forEach(function (childSnapshot){
							barcode=childSnapshot.key;
							price=0;
							name='';
							quantity=0;
							childSnapshot.forEach(function(subChild){
								if(subChild.key.localeCompare('price')==0){
									price=subChild.val();
								}
								if(subChild.key.localeCompare('name')==0){
									name=subChild.val().toString();
								}
								if(subChild.key.localeCompare('quantity')==0){
									quantity=subChild.val();
								}
					
							});
							
							fillRow_change(tableName,name,price,quantity,barcode);
							
						});
						
						return resolve();
					});
					
				});	
			}

			//gets name of shop from db

			function getName(id){

				return new Promise(function (resolve, reject){
					var ref = firebase.database().ref(id);
					ref.once('value').then(function(snap){	
						snap.forEach(function (childSnapshot){
							if(childSnapshot.key.localeCompare('name')==0){
									nameOfShop=childSnapshot.val();
									console.log(nameOfShop);
									return resolve();			
							}
						});
						return reject();
					});
					
				});		
				
			}

			//called when order is placed

			function place(e){
				e.preventDefault();
				document.getElementById('submitButton').style.opacity=0.3;
				checkoutID=document.getElementById('checkoutID').value;
				id=sessionStorage.shopID;
				verifyCheckoutID(id,checkoutID)
				.then(function(){
					update(id,checkoutID)
					.then(function(){
						console.log('updated');
					})
					.catch(function(){

					})
					location.reload();
				})
				.catch(function(){
					document.getElementById('submitButton').setAttribute("data-toggle","modal");
					$("#myModal").modal("show");
				});
				document.getElementById('submitButton').style.opacity=1;

			}

			//updates the db on placing the order
			function update(id,checkoutID){
				console.log('in update');
				return new Promise(function (resolve, reject){
					var addr=id.concat("/customers/");
					addr=addr.concat(checkoutID.toString());
					addr1=addr.concat("/status");
					var ref = firebase.database().ref(addr1);
					ref.set('active');

					var d = new Date(); 
					h=d.getHours().toString();
					m=d.getMinutes().toString(); 
					s=d.getSeconds().toString();
					time="";
					time=time.concat(h+":");
					time=time.concat(m+":");
					time=time.concat(s);	

					addr2=addr.concat("/time");
					ref = firebase.database().ref(addr2);
					ref.set(time);

					return resolve();
					
				});

			}


			function checkStatus(id,checkoutID){
				return new Promise(function (resolve, reject){
					var addr=id.concat("/customers/");
					addr=addr.concat(checkoutID.toString());
					var ref = firebase.database().ref(addr);
					ref.once('value').then(function(snap){
						snap.forEach(function(childSnapshot){
							if(childSnapshot.key.localeCompare('status')==0){
								if(childSnapshot.val().localeCompare('passive')==0)
									return resolve();
							}
						})
						return reject();
					});
					
				});
			}

			//checks whether checkout id written in the place checkbox is valid
			function verifyCheckoutID(id,checkoutID){

				checkVal=0;

				return new Promise(function (resolve, reject){
					var addr=id.concat("/customers");
					var ref = firebase.database().ref(addr);



					ref.once('value').then(function(snap){

						if(snap.hasChild(checkoutID))
							;
						else
							return reject();

						snap.forEach(function (childSnapshot){

							if(childSnapshot.key==checkoutID){
									checkStatus(id,checkoutID)
									.then(function(){
										console.log('valid');
										return resolve();
									})
									.catch(function(){
										console.log('invalid');
										return reject();
									})
									
							}
	
								//return reject();
							
						});
						console.log(checkVal);
						//console.log(checkVal);	
						//if(checkVal==1)
							

						//return reject();
					});

					console.log('out');

				});
			}

			//called when shopkeeper wants to update his collection
			function change(e){
				
				$("#changeTable td").remove();
				
				console.log('any');
				id=sessionStorage.shopID;
				fillTable_change('changeTable',id)
					.then(function(){
						document.body.style.opacity=1;
						document.getElementById("loader").remove();
					})
					.catch(function(){
						;
					});
				document.getElementById('change').setAttribute("data-toggle","modal");
				$("#myModal_change").modal("show");

			}

			function addItem(){
				sessionStorage.editStatus="add";
				$("#myModal_edit").modal("show");
			}

			function handleFiles(files){
				getText(files[0]);
			}

			function getText(fileToRead){
				var reader=new FileReader();
				reader.readAsText(fileToRead);
				reader.onload=loadHandler;
			}
			function loadHandler(){
				var csv=event.target.result;
				processData(csv);
			}
			function processData(csv){
				var lines=csv.split('\n');
				var barCodeList=[];
				var list1=[];
				for(var i=0;i<lines.length;i++){
					var myObject = {};
					var data=lines[i];
					data=data.trim();
					var store=data.split(',');
					myObject.name=store[0];
					myObject.price=store[1];
					myObject.quantity=store[2];
					//var name=store[0];
					//var quantity=store[1];
					//var price=store[2];
					list1.push(myObject);
					barCodeList.push(store[3]);
				}
				editDB_csv(barCodeList,list1);
			}

			function myFunc (barCodeList,list1) {
				
				return new Promise(function (resolve, reject){
					var id=sessionStorage.shopID;
					var addr=id.concat('/items');
					var refer = firebase.database().ref(addr);
					var i=0;
					for(var i=0;i<list1.length;i++){
						var name1=list1[i].name.toString();
						var price1=list1[i].price.toString();
						var quantity1=list1[i].quantity.toString();
						
						var elemBarcode=barCodeList[i];
						
						try{
							refer.child(elemBarcode).set({name:name1,
							price:price1,
							quantity:quantity1});
						}
						catch(err)
						{
							console.log(err.message);
						}

					}


					//ans={name:"ghkug",price:"32",quantity:"21"};
					//ref.child("777").set(ans);
					return resolve();
				});
			}

			function editDB_csv(barCodeList,list1){

				//console.log(myObject);
				var id=sessionStorage.shopID;
				var addr=id.concat("/items/");
				var ref = firebase.database().ref(addr);
				
				myFunc(barCodeList,list1)
					.then(function(){
						
					})
					.catch(function(){
						;
					});
	
			} 

			function exhaust(){
				id=sessionStorage.shopID;
				var addr=id.concat("/items");
				var ref = firebase.database().ref(addr);
				checkForExhaustingItems(ref)
				.then(function(){
					//document.getElementById('').setAttribute("data-toggle","modal");
					$("#myModal_exhaust").modal("show");
				})
				.catch(function(){
					;
				});
			}

			function checkForExhaustingItems (ref) {
				var limit=10;
				return new Promise(function (resolve, reject){
					ref.once('value').then(function(snap){
						snap.forEach(function(childSnapshot){
							var prod_barcode=childSnapshot.key;
							var prod_name='';
							var prod_price=0;
							var prod_quantity=0;
							var isCorrect=0;
							childSnapshot.forEach(function(subChild){
								if(subChild.key.localeCompare("name")==0)
									prod_name=subChild.val();
								if(subChild.key.localeCompare("price")==0)
									prod_price=subChild.val();	

								if(subChild.key.localeCompare("quantity")==0){
									//console.log(subChild.val());
									prod_quantity=subChild.val();
									if(parseInt(subChild.val())<limit){
										isCorrect=1;
									}
								}
							})
							if(isCorrect==1)
								fillRow_change("exhaustTable",prod_name,prod_price,prod_quantity,prod_barcode);
						})
						return resolve();
					});
				});
			}