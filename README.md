# Kit API Integration

## Description
I created this application to extend the functionality beyond what was possible for someone using Kit's UI. Kit is a platform for email marketing that I worked with as a volunteer for a nonprofit. Out of the box, Kit did not have the functionality the nonprofit needed for their use cases, so I created this application to integrate with Kit's API, automate their community engagement tasks, and save volunteer time, which was crucial for a nonprofit.

The main use case for this app was to supply a list of emails to Kit, tag them if they exist in the mailing list and NOT add them to the mailing list if they do not. The Kit web app only offered a way to add a list of emails via csv, and optionally add them. This app also lists all the subscribers

The app asks the user to supply the API key rather pulling a stored key. By asking for a key, this application becomes scalable, allowing for the management of multiple Kit accounts.

## Technology 
frontend = React.js
backend = Java and Springboot

## Local deployment
To start frontend:
from src folder: 
npm install (if necessary)
npm run dev

To start backend:
from backend folder: 
./gradlew bootRun