Main use case for this app was that I wanted a way to supply a list of emails to Kit, tag them if they exist in the mailing list
and NOT add them to the mailing list if they do not. The Kit web app only offered a way to add a list of emails via csv, and
optionally add them

This app also lists all the subscribers related to the provied API Key

frontend = react
backend = spring and java
frontend asks user for Kit api key and sends it to backend

To start frontend:
from src folder: 
npm install (if necessary)
npm run dev

To start backend:
from backend folder: 
./gradlew bootRun