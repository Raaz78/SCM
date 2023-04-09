console.log("This is script file");

const toggleSidebar=()=>{
if($('.sidebar').is(":visible"))
{
    $('.sidebar').css("display","none");
    $('.content').css("margin-left","0%");
}
else{
    $('.sidebar').css("display","block");
    $('.content').css("margin-left","20%");
}
};

const search=()=>{
//console.log("called");
let query = $("#search-input").val();

if(query=="")
{
    $(".search-result").hide();
}
else{
    //search
    console.log(query);

    let url = `http://localhost:8080/search/${query}`;
    fetch(url).then(response=>{
       return response.json();  
    })
    .then((data)=>{
        //data....
        console.log(data);
        let text= `<div class='list-group'>`
        data.forEach(contact => {
            text+= `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`
        });
        text+=`</div>`;
        $(".search-result").html(text);
        $(".search-result").show();
    });


}
};

//first request to serve the create order

const paymentStart=()=>
{
    console.log("payment started..");
    let amount = $("#payment_field").val();
    console.log(amount);
    if(amount=="" || amount==null)
    {
       // alert("amount is required !!");
        swal("Failed !!", "Amount is required !!", "error");
        return;
    }

    //code
    //we will use ajax to send request to server to create order - jquery 
    $.ajax
    (
        {
            url:'/user/create-order',
            data: JSON.stringify({amount:amount,info:"order_request"}),
            contentType:"application/json",
            type:"POST",
            dataType:"json",
            success:function(response)
            {
                //invoke when success
                console.log(response);
                if(response.status == "created")
                {
                    let options =
                    {
                        key:'key',
                        amount:response.amount,
                        currency:'INR',
                        name:'Smart Contact Manager',
                        description:'Donation',
                        image:'data:image/jpeg;base64,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',
                        order_id:response.id,
                        handler:function(response)
                        {
                            console.log(response.razorpay_payment_id);
                            console.log(response.razorpay_order_id);
                            console.log(response.razorpay_signature);
                            console.log('payment successful !!')
                            //alert("Congrates!! Payment Successful !!")
                            updatePaymentOnServer
                            (
                                response.razorpay_payment_id,
                                response.razorpay_order_id,
                                'paid'
                            );
                        },
                        prefill: {
                            name: "",
                            email: "",
                            contact: "",
                        },
                        notes: 
                        {
                            address: "Raj's Website",
                        },
                        theme:
                        {
                            color: "#3399cc",
                        },
                    };

                    let rzp = new  Razorpay(options);
                    rzp.on('payment.failed', function (response)
                    {
                        console.log(response.error.code);
                        console.log(response.error.description);
                        console.log(response.error.source);
                        console.log(response.error.step);
                        console.log(response.error.reason);
                        console.log(response.error.metadata.order_id);
                        console.log(response.error.metadata.payment_id);
                        //console.log("oops payment failed");
                        swal("Failed !!", "Oops payment failed", "error");
                    });
                    rzp.open();
                }

            },
            error:function(error)
            {
                //invoke when error
                console.log(error);
               // alert("something went wrong!!");
                swal("Sorry !!", "Something went wrong!!", "error");
            },
        }
    );
}; 

//
function updatePaymentOnServer(payment_id,order_id,status)
{
    $.ajax
    ({
        url:'/user/update-order',
        data: JSON.stringify({payment_id:payment_id,order_id:order_id,status:status}),
        contentType:"application/json",
        type:"POST",
        dataType:"json", 
        success:function(response)
        {
            swal("Good job!", "Congrates!! Payment Successful !!", "success");
        },
        error:function(error) 
        {
            swal("Done !!", "Your Payment is successful, but we didn't get on server, we will contact you asap !!", "error");
        },
    });
}