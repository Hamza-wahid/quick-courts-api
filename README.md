# QuickCourts
## API
The tennis club can sign up and register members at 3 tiers of membership (Basic, Premium and VIP) 
with the tier determining the maximum court time a member can book per day in minutes. Currently, this is as follows but can be easily configured: 

   * Standard = 60 minutes
   * Premium = 120 minutes
   * VIP = 150 minutes
    
    
Members are able to create, cancel and modify bookings

### End Points
All responses will be in JSON with appropriate Status codes as per REST semantics.

###### POST auth/register
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
    201 Created on Success
    409 Conflict - User has already registered
    400 Bad Request on Failure - Password/email are in an incorrect format
    
  ######  POST auth/login
    
    Arguments
            
        {
            "email": "bob@gmail.com",
            "password": "aPassword123"
        }

    Response
    200 OK on Success
    401 Unauthorized - Invalid credentials
    
   ###### GET /booking?date=yyyy-MM-dd
   
    Date parameter must be in the format specified 
    


   ###### GET booking/{id}
   
    Response
 
    {
    "courtNumber": 1,
    "date": "2020-02-01",
    "startTime": "13:00",
    "endTime": "14:00"
    }
    
    200 OK on Success
    404 Not Found
    
   ###### POST /booking
   
       Arguments
        
    {
        "courtNumber": 1,
        "startDateTime": "2021-01-01 12:00",
        "endDateTime": "2021-01-01 12:30"
    }
    
    Response
    
    {
         "bookingId": 12
    }
    
    201 Created on Success
    409 Confict - Slot not Available
    401 Unauthorized - User is not authorized to make this booking
    
    
   ###### PATCH booking/id?courtNumber=3
    
    Response
    204 No Content on Success
    409 Conflict - New court not available
    401 Unauthorized - User is not authorized to modify this booking

    
   ###### DELETE booking/id
   
    Response
    204 No Content on Success
    404 Not Found - Booking does not exist
    401 Unauthorized - User is not authorized to cancel this booking

    
