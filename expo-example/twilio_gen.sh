#!/bin/sh

# Get API_SID and API_SECRET from Twilio Console
# Run this script to generate twilio tokens for env.ts, and create a room
# Ex how to use: API_SID=API_SID_FROM_CONSOLE API_SECRET=API_SECRET_FROM_CONSOLE ./twilio_gen.sh

API_SID=$API_SID
API_SECRET=$API_SECRET
UNIQUE_ROOM_NAME="TestRoom001"
IDENTITY_1="User001"
IDENTITY_2="User002"

twilio token:video --identity=$IDENTITY_1
twilio token:video --identity=$IDENTITY_2

curl -XPOST 'https://video.twilio.com/v1/Rooms' \
  -u "$API_SID:$API_SECRET" \
  -d "UniqueName=$UNIQUE_ROOM_NAME"

