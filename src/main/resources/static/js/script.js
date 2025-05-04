console.log("UrbanDrip js");

$(function() {

    // User Register validation

    var $userRegister = $("#userRegister");

    $userRegister.validate({

        rules: {
            name: {
                required: true,
                lettersonly: true
            },
            email: {
                required: true,
                space: true,
                email: true
            },
            mobileNumber: {
                required: true,
                space: true,
                numericOnly: true,
                minlength: 10,
                maxlength: 12

            },
            password: {
                required: true,
                space: true

            },
            confirmPassword: {
                required: true,
                space: true,
                equalTo: '#pass'

            },
            address: {
                required: true,
                all: true

            },
            city: {
                required: true,
                space: true

            },
            state: {
                required: true

            },
            pincode: {
                required: true,
                space: true,
                numericOnly: true

            },
            file: {
                required: true
            }

        },
		
		messages: {
		    name: {
		        required: 'Name is required',
		        lettersonly: 'Please enter a valid name (letters only)'
		    },
		    email: {
		        required: 'Email is required',
		        space: 'Email cannot contain spaces',
		        email: 'Please enter a valid email address'
		    },
		    mobileNumber: {
		        required: 'Mobile number is required',
		        space: 'Mobile number cannot contain spaces',
		        numericOnly: 'Please enter a valid mobile number (numbers only)',
		        minlength: 'Mobile number must be at least 10 digits',
		        maxlength: 'Mobile number cannot exceed 12 digits'
		    },
		    password: {
		        required: 'Password is required',
		        space: 'Password cannot contain spaces'
		    },
		    confirmPassword: {
		        required: 'Please confirm your password',
		        space: 'Confirm password cannot contain spaces',
		        equalTo: 'Passwords do not match'
		    },
		    address: {
		        required: 'Address is required',
		        all: 'Please enter a valid address'
		    },
		    city: {
		        required: 'City is required',
		        space: 'City cannot contain spaces'
		    },
		    state: {
		        required: 'State is required',
		        space: 'State cannot contain spaces'
		    },
		    pincode: {
		        required: 'Pincode is required',
		        space: 'Pincode cannot contain spaces',
		        numericOnly: 'Please enter a valid pincode (numbers only)'
		    },
		    file: {
		        required: 'Please upload an image'
		    }
		}

    });

	// Reset Password Validation

	var $resetPassword=$("#resetPassword");

	$resetPassword.validate({
			
			rules:{
				password: {
					required: true,
					space: true

				},
				confirmPassword: {
					required: true,
					space: true,
					equalTo: '#pass'

				}
			},
			messages:{
			   password: {
					required: 'password must be required',
					space: 'space not allowed'

				},
				confirmPassword: {
					required: 'confirm password must be required',
					space: 'space not allowed',
					equalTo: 'password mismatch'

				}
			}	
	})
	
	// Orders Validation

	var $orders=$("#orders");

	$orders.validate({
			rules:{
				firstName:{
					required:true,
					lettersonly:true
				},
				lastName:{
					required:true,
					lettersonly:true
				}
				,
				email: {
					required: true,
					space: true,
					email: true
				},
				mobileNumber: {
					required: true,
					space: true,
					numericOnly: true,
					minlength: 10,
					maxlength: 12

				},
				address: {
					required: true,
					all: true

				},

				city: {
					required: true,
					space: true

				},
				state: {
					required: true,


				},
				pincode: {
					required: true,
					space: true,
					numericOnly: true

				},
				paymentType:{
				required: true
				}
			},
			messages:{
				firstName:{
					required:'first required',
					lettersonly:'invalid name'
				},
				lastName:{
					required:'last name required',
					lettersonly:'invalid name'
				},
				email: {
					required: 'email name must be required',
					space: 'space not allowed',
					email: 'Invalid email'
				},
				mobileNumber: {
					required: 'mob no must be required',
					space: 'space not allowed',
					numericOnly: 'invalid mob no',
					minlength: 'min 10 digit',
					maxlength: 'max 12 digit'
				}
			   ,
				address: {
					required: 'address must be required',
					all: 'invalid'

				},

				city: {
					required: 'city must be required',
					space: 'space not allowed'

				},
				state: {
					required: 'state must be required',
					space: 'space not allowed'

				},
				pincode: {
					required: 'pincode must be required',
					space: 'space not allowed',
					numericOnly: 'invalid pincode'

				},
				paymentType:{
				required: 'select payment type'
				}
			}	
	})
	
    jQuery.validator.addMethod('lettersonly', function(value, element) {
        return /^[^-\s][a-zA-Z_\s-]+$/.test(value);
    });

    jQuery.validator.addMethod('space', function(value, element) {
        return /^[^-\s]+$/.test(value);
    });

    jQuery.validator.addMethod('all', function(value, element) {
        return /^[^-\s][a-zA-Z0-9_,.\s-]+$/.test(value);
    });

    jQuery.validator.addMethod('numericOnly', function(value, element) {
        return /^[0-9]+$/.test(value);
    });

});
