# coding:utf8

import requests
import json
import base64
import time
from requests.packages.urllib3.exceptions import InsecureRequestWarning
# 禁用安全请求警告
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

result_set = set([])

AUTO_URL = 'https://wdpush.sogoucdn.com/api/anspush?key=cddh&wdcallback=jQuery200012885665125213563_1518087319437&_=1518087319438'


def getAutoValue():
    headers = {
        'Host': 'wdpush.sogoucdn.com',
        'Connection': 'keep-alive',
        'Connection': 'keep-alive',
        'Accept': '*/*',
        'User-Agent': 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_5 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/13G36 Sogousearch/Ios/5.9.7',
        'Accept-Language': 'zh-cn',
        'Referer': 'https://assistant.sogoucdn.com/v5/cheat-sheet?channel=hj&icon=http%3A%2F%2Fapp.sastatic.sogoucdn.com%2Fdati%2Fhj.png&name=%E7%99%BE%E4%B8%87%E8%B5%A2%E5%AE%B6&appName=%E8%8A%B1%E6%A4%92%E7%9B%B4%E6%92%AD%2F%E5%BF%AB%E8%A7%86%E9%A2%91',
        'Accept-Encoding': 'gzip, deflate',
        'Cookie': 'APP-SGS-ID=e6ccf289bbd571f66fc55009dd13711c4e02924f5ddc'
    }
    r = requests.get(headers=headers, url=AUTO_URL, verify=False)
    html = r._content
    html = html[html.find("(") + 1: len(html) - 1]
    return json.loads(base64.b64decode(json.loads(html)['result']))


#展示题目和答案
def display(value):
    try:
        json_obj = json.loads(value)
        print json_obj['title']
        if len(json_obj['answers']) > 0:
            print u"A." + json_obj['answers'][0] + "  B." + json_obj['answers'][1] + "  C." + json_obj['answers'][2]
            print u"==================="
            print u"分析: " + json_obj['search_infos'][0]['summary']
            print u"==================="
            print u"推荐答案：  " + json_obj['recommend']
        print ""
    except:
        print u"本题出错!"
        pass


#使用搜狗搜索（https://www.sogou.com/）的api自动解答
while(True):
    for value in getAutoValue():
        if value not in result_set:
            result_set.add(value)
            display(value)
    time.sleep(0.5)
