import firebase_admin
from firebase_admin import messaging
import mysql.connector
import requests
import task
import os
os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = '/home/ubuntu/iems5722/privatekey.json'

conn = mysql.connector.connect(
        host = 'localhost',
        user = "userEllyn",
        password = "213619zyl",
        database = "iems5722",
        )

default_app = firebase_admin.initialize_app()


def notify_all_users_for_new_message(chatroom_id, new_message):
    query = "SELECT DISTINCT token FROM push_tokens"
    conn.ping(reconnect=True)
    cursor = conn.cursor(dictionary = True)
    cursor.execute(query)
    tokens = cursor.fetchall()
    for tok in tokens:
        registration_token = tok.get('token')   
        message = messaging.Message(
            data={
                'chatroom_id': str(chatroom_id),
                'new_message': new_message,
            },
            token=registration_token,
        )

        response = messaging.send(message)
        print('Successfully sent message:', response)
