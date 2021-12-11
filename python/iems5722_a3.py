from flask import Flask,Response,request, jsonify
import mysql.connector;
import json
import time
from task import sendNotification
# 连接数据库
# db = mysql.connector.connect(
#     host="localhost",
#     user="dbuser",
#     passwd="password",
#     database="iems5722"
# )
app = Flask(__name__)
# 开始事务
# db.start_transaction()

# 获取游标对象: CMySQLCursor
# cursor = db.cursor(dictionary=True)
@app.route('/')
def hello_world():  # put application's code here
    return 'Hello World!'

@app.route('/api/a3/get_chatrooms', methods=['GET'])
def get_chatrooms():  # put application's code here
    try:
        db = mysql.connector.connect(
            host="localhost",
            user="dbuser",
            passwd="password",
            database="iems5722"
        )
        cursor = db.cursor(dictionary=True)
        cursor.execute("select * from chatrooms;")
        classrooms = cursor.fetchall()
        if len(classrooms)!=0:
            return_chatroom = {}
            data = []
            for classroom in classrooms:
                data.append(classroom)
            return_chatroom['data'] = data
            return_chatroom['status'] = "OK"
            cursor.close()
            db.close();
            return Response(json.dumps(return_chatroom), mimetype='application/json')
        else:
            cursor.close()
            db.close();
            return Response(json.dumps({"message": "<error message>", "status": "ERROR"}), mimetype='application/json')
    except Exception as err:
        cursor.close()
        db.close();
        return Response(json.dumps({"message": str(err),"status": "ERROR"}), mimetype='application/json')

@app.route('/api/a3/get_messages', methods=['GET'])
def get_messages():
    chatroom_id = request.args.get("chatroom_id")
    page = request.args.get("page")
    try:
        db = mysql.connector.connect(
            host="localhost",
            user="dbuser",
            passwd="password",
            database="iems5722"
        )
        cursor = db.cursor(dictionary=True)
        if chatroom_id and page:
            cursor.execute("select message,name,date_format(message_time, '%Y-%m-%d %H:%i:%s') as message_time,user_id from messages WHERE chatroom_id="+chatroom_id+" ORDER BY id DESC LIMIT 5 OFFSET "+page+";")
            messages = cursor.fetchall()
            if len(messages)!=0:
                cursor.execute("SELECT COUNT(*) as total_pages FROM messages")
                total_pages = cursor.fetchone()
                if len(total_pages) != 0:
                    data_tuple = {}
                    datas = {}
                    data_tuple['current_page'] = page;
                    message_list = []
                    for item in messages:
                        message_list.append(item)
                    data_tuple["messages"] = message_list
                    data_tuple["total_pages"] = total_pages["total_pages"]
                    datas['data'] = data_tuple
                    datas['status'] = "OK"
                    cursor.close()
                    db.close();
                    return Response(json.dumps(datas), mimetype='application/json')
                else:
                    cursor.close()
                    db.close();
                    return Response(json.dumps({"message": "<error message>", "status": "ERROR"}),mimetype='application/json')
            else:
                cursor.close()
                db.close();
                return Response(json.dumps({"message": "<error message>", "status": "ERROR"}), mimetype='application/json')
        else:
            cursor.close()
            db.close();
            return Response(json.dumps({"message": "Enter the required fields", "status": "ERROR"}),mimetype='application/json')
    except Exception as err:
        cursor.close()
        db.close();
        return Response(json.dumps({"message": str(err),"status": "ERROR"}), mimetype='application/json')

@app.route('/api/a3/send_message', methods=['POST'])
def send_message():
    try:
        db = mysql.connector.connect(
            host="localhost",
            user="dbuser",
            passwd="password",
            database="iems5722"
        )
        cursor = db.cursor(dictionary=True)
        data_postParam = request.form
        chatroom_id = data_postParam.get("chatroom_id")
        user_id = data_postParam.get("user_id")
        name = data_postParam.get("name")
        message = data_postParam.get("message")
        message_time = time.strftime("%Y-%m-%d %H:%M", time.localtime())
        print("chatroom_id: ",chatroom_id," user_id: ",user_id," name:",name," message: ",message)
        if chatroom_id and user_id and name and message:
            sql = "INSERT INTO messages (chatroom_id,user_id,name,message,message_time) VALUES (%s, %s, %s, %s, %s)"
            val = (chatroom_id, user_id, name, message, message_time)
            cursor.execute(sql, val)
            db.commit()
            print("Success", cursor.lastrowid)
            # db.commit()
            # ----------------------------------------------------------------------------------------
            sql = "select * from push_tokens"
            cursor.execute(sql)
            user_tokens = cursor.fetchall()
            print("User_tokens: ",user_tokens)
            carry = []
            
            if len(user_tokens) != 0:
                print(len(user_tokens))
                for row in user_tokens:
                    tokens = row['token']
                    carry.append(str(tokens))
                    print("carry:", carry)
            print("----------------------------------")
            sql = "select chatrooms.name from chatrooms where chatrooms.id = " + str(chatroom_id)
            cursor.execute(sql)
            result_names = cursor.fetchall()
            chatroom_name = ''
            print("***************************************")
            if len(result_names) != 0:
                for row in result_names:
                    chatroom_name = row['name']
            # # ----------------------------------------------------------------------------------------
            cursor.close()
            db.close();
            print("+++++++++++++++++++++++++++")
            sendNotification.delay(carry, str(chatroom_id),chatroom_name, message)
            data = {"status": "OK"}
            return Response(json.dumps(data), mimetype='application/json')
        else:
            cursor.close()
            db.close();
            return Response(json.dumps({"message": "Enter the required fields","status": "ERROR"}), mimetype='application/json')
    except Exception as err:
        print(err)
        print("I come here err --- Hao ge!")
        cursor.close()
        db.close();
        return Response(json.dumps({"message": str(err),"status": "ERROR"}), mimetype='application/json')

@app.route('/api/a4/submit_push_token', methods=['POST'])
def get_token():
    try:
        db = mysql.connector.connect(
            host="localhost",
            user="dbuser",
            passwd="password",
            database="iems5722"
        )
        cursor = db.cursor(dictionary=True)
        postParam = request.form
        user_id = postParam.get("user_id")
        token = postParam.get("token")
        sql = "INSERT INTO push_tokens (user_id, token) VALUES (%s, %s)"
        val = (user_id, token)
        cursor.execute(sql, val)
        db.commit()
        print("Success", cursor.lastrowid)
        data = {"status": "OK","token":token}
        cursor.close()
        db.close();
        return Response(json.dumps(data), mimetype='application/json')
    except Exception as err:
        cursor.close()
        db.close();
        return Response(json.dumps({"message": str(err), "status": "ERROR"}), mimetype='application/json')

if __name__ == '__main__':
    app.run(host='0.0.0.0',port='6000')
    # , debug = True
