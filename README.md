# QuickCourts

### Summary
The tennis club can sign up and register members at 3 tiers of membership (Basic, Premium and VIP) 
with the tier determining the maximum court time a member can book per day in minutes. Currently, this is as follows but can be easily configured: 

    * Standard = 60 minutes
    * Premium = 120 minutes
    * VIP = 150 minutes
    
    
Members are able to create, cancel and modify bookings

### End Points
All responses will be in JSON with appropriate Status codes:
#### Auth (  /auth   )

###### POST /register
    Arguments
        
    {
        "email": "bob@gmail.com",
        "password": "aPassword123",
        "firstName": "Bob",
        "lastName": "John",
        "gender": 1,
        "membershipType": 3
    }
    
    Response
        202 OK on Success
        400 Bad Request on Failure
    
  ######  POST /login
    
    Arguments
            
        {
            "email": "bob@gmail.com",
            "password": "aPassword123"
        }
    
    
##### Booking /booking

    GET api/booking?date=
        
    GET api/booking/id
    
    
    POST api/booking
    
    
    PATCH api/booking/id?courtNumber=
    
    
    DELETE api/booking/id
        


    
