from flask import Flask

app = Flask(__name__)
app.config.update(
CELERY_BROKER_URL='amqp://guest@localhost//'
)

from flask import jsonify
from flask import request
from datetime import datetime
from celery import Celery
import math
import mysql.connector
import requests
import task

def make_celery(app):
    celery = Celery(
        app.import_name,
        broker=app.config['CELERY_BROKER_URL']
    )
    celery.conf.update(app.config)
    
    class ContextTask(celery.Task):
        def __call__(self, *args, **kwargs):
            with app.app_context():
                return self.run(*args, **kwargs)
    celery.Task = ContextTask
    
    return celery

celery = make_celery(app)

conn = mysql.connector.connect(
        host = 'localhost',
        user = "userEllyn",
        password = "213619zyl",
        database = "iems5722",
        )

@app.route('/')
def homepage():
    return "Welcome to Ellyn's home."

@app.route("/api/a3/get_chatrooms")
def get_chatrooms():
    query1 = "SELECT * FROM chatrooms"
    mychatroom = {}
    conn.ping(reconnect=True)
    cursor = conn.cursor(dictionary = True)
    cursor.execute(query1)
    mychatroom["status"] = "OK"
    mychatroom["data"] = cursor.fetchall()
    return jsonify(mychatroom)    
    
    
@app.route('/api/a3/get_messages',methods=['GET'])
def get_messages():
    query2 = "SELECT COUNT(message) FROM messages WHERE chatroom_id = %s"
    query3 = "SELECT id,chatroom_id, user_id, name, message, message_time FROM messages WHERE chatroom_id = %s ORDER BY message_time DESC LIMIT %s,4"
    mymessage = {}
    chatroom_id = int(request.args.get('chatroom_id'))
    page = int(request.args.get('page'))
    
    #获取消息总数total
    conn.ping(reconnect=True)
    cursor = conn.cursor(dictionary = True)
    cursor.execute(query2,(chatroom_id,))
    total = cursor.fetchall()[0]["COUNT(message)"]
    
    #判断如果输入不符合规定就返回Error
    if page <= 0 or math.ceil(total / 4) < page:
        mymessage["message"] = "<error message>"
        mymessage["status"] = "ERROR"
    else:
        mymessage["status"] = "OK"
        mymessage["data"] = {}
        mymessage["data"]["current_page"] = page
        mymessage["data"]["messages"] = []
        
        #将消息按照时间顺序排列，每页有四条消息，limit范围为（(页数-1)*5后面的5个)
        cursor.execute(query3,(chatroom_id,(page - 1)*4))

        for msg in cursor.fetchall():
            mymessage["data"]["messages"].append(msg)

        mymessage["data"]["total_pages"] = math.ceil(total / 4)
        
    return jsonify(mymessage)

@app.route('/api/a4/submit_push_token',methods=['POST'])
def post_token():
    query0 = 'INSERT INTO push_tokens (user_id, token) VALUES (%s,%s)'
    mytoken = {}
    user_id = int(request.form.get("user_id"))
    token = request.form.get("token")
    if user_id ==None or token == None:
        mytoken["status"]="ERROR"
    else:
        mytoken["status"]="OK"
        conn.ping(reconnect=True)
        cursor = conn.cursor(dictionary=True)
        cursor.execute(query0,(user_id,token))
        conn.commit()
    
    return jsonify(mytoken)

@app.route("/api/a3/send_message", methods=['POST'])
def send_messages():
    mysendmsg = {}
    chatroom_id = int(request.form.get("chatroom_id"))
    user_id = int(request.form.get("user_id"))
    name = request.form.get("name")
    message = request.form.get("message")
    #print(timestamp)

    if chatroom_id < 1 or chatroom_id > 3 or message == None or chatroom_id == None or name == None or user_id == None:
        mysendmsg["message"] = "<error message>"
        mysendmsg["status"] = "ERROR"
    else:
        mysendmsg["status"]="OK"
        conn.ping(reconnect=True)
        cursor = conn.cursor(dictionary=True)
        query4 = "INSERT INTO messages (chatroom_id, user_id, name, message) VALUES (%s,%s,%s,%s)"
        cursor.execute(query4,(chatroom_id, user_id, name, message))
        conn.commit()

    notify_all_users_for_new_message.delay(chatroom_id, message)
    return jsonify(mysendmsg)

@celery.task()
def notify_all_users_for_new_message(chatroom_id, new_message):
    task.notify_all_users_for_new_message(chatroom_id, new_message)

if __name__ == '__main__':
    app.debug = False
    app.run(host='0.0.0.0',port=8000)


