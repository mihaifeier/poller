# How to run
1. Go into the folder `frontend` and run `docker build -t poller-react:latest .`
2. Go into the folder `backend` and run `docker build -t poller-service:latest .`
3. Go into the folder `docker` and run `docker compose up`
4. Open the frontend application at: http://localhost:3000

Note: Wait for the application to fully start, otherwise you may encounter `Thread has been blocked for X ms` errors.

# Extra info
1. If multiple users use the same name at the same time, the live updates will only work for the last user that logged in
2. The status of a new poll is 'FAIL' by default
3. The poller runs every 5 seconds
4. A valid URL must contain `http://` or `https://`

# Basic requirements
1. [x] A user needs to be able to add a new service with URL and a name
2. [x] Added services have to be kept when the server is restarted
3. [x] Display the name, url, creation time and status for each service
4. [x] Provide a README in english with instructions on how to run the
   application

# Extra requirements
1. [x] We want full create/update/delete functionality for services
2. [x] The results from the poller are automatically shown to the user (no
   need to reload the page to see results)
3. [x] We want to have informative and nice looking animations on
   add/remove services
4. [x] The service properly handles concurrent writes
5. [x] Protect the poller from misbehaving services (for example answering really slowly)
6. [x] URL Validation ("sdgf" is probably not a valid service)
7. [x] Multi user support. Users should not see the services added by
   another user

# Future improvements 
1. Authentication
2. PollerVerticle caches the URLs on start and gets updates using the event bus instead of always retrieving them from the DB
3. Validations + permission checking