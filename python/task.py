from celery import Celery
import datetime
import firebase_admin
from firebase_admin import messaging
from firebase_admin import credentials
celery = Celery('task', broker='amqp://guest@localhost//')
cred = credentials.Certificate("t-tide-325516-firebase-adminsdk-vfazt-bacb07830f.json")
firebase_admin.initialize_app(cred)
@celery.task
def sendNotification(TokenList, Id,Name,Msg):
    Registration_tokens = TokenList
    print("Registration_tokens: ",Registration_tokens)

    message = messaging.MulticastMessage(
        android=messaging.AndroidConfig(
            ttl=datetime.timedelta(seconds=3600),
            priority='normal',
            data={
                "Id":Id,
                "Title":Name,
                "Msg":Msg
            }
        ),
        tokens=Registration_tokens,
    )
    print('Celery.task!!!!!')
    response = messaging.send_multicast(message)
    if response.failure_count > 0:
        responses = response.responses
        failed_tokens = []
        for idx, resp in enumerate(responses):
            if not resp.success:
                # The order of responses corresponds to the order of the registration tokens.
                failed_tokens.append(Registration_tokens[idx])
        print('List of tokens that caused failures: {0}'.format(failed_tokens))
    print('{0} messages were sent successfully'.format(response.success_count))
