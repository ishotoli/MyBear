# MyBear
Java高仿FastDFS

#### 2017-04-08 协议解析
命令：
* FDFS_PROTO_CMD_QUIT      = 82;                                 //关闭socket时的通知消息
* TRACKER_PROTO_CMD_SERVER_LIST_GROUP     = 91;                  //列举所有group
* TRACKER_PROTO_CMD_SERVER_LIST_STORAGE   = 92;                  //列举所有storage
* TRACKER_PROTO_CMD_SERVER_DELETE_STORAGE = 93;                  //从tracker移除指定storage
* TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE = 101; //不带groupname的查询上传storage
* TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ONE = 102;               //查询下载storage
* TRACKER_PROTO_CMD_SERVICE_QUERY_UPDATE = 103;                  //查询更新操作storage（删除文件或者设置meta）
* TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE = 104;    //带groupname查询上传storage
* TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ALL = 105;               //获取所有可以下载的storage
* TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ALL = 106; //获取所有可以上传的storage，不指定groupname
* TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ALL = 107;    //获取所有可以上传的storage，指定groupname
* TRACKER_PROTO_CMD_RESP = 100;                                  //服务器返回消息命令
* FDFS_PROTO_CMD_ACTIVE_TEST = 111;                              //心跳，注意返回也是resp
* STORAGE_PROTO_CMD_UPLOAD_FILE  = 11;                           //文件上传到storage
* STORAGE_PROTO_CMD_DELETE_FILE	= 12;                           //从storage删除文件
* STORAGE_PROTO_CMD_SET_METADATA	 = 13;                          //设置metadata
* STORAGE_PROTO_CMD_DOWNLOAD_FILE = 14;                          //从storage下载文件
* STORAGE_PROTO_CMD_GET_METADATA	 = 15;                          //获取metadata
* STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE   = 21;                    //上传文件到slave
* STORAGE_PROTO_CMD_QUERY_FILE_INFO     = 22;                    //查询文件信息
* STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE= 23;                    //上传一个可追加文件
* STORAGE_PROTO_CMD_APPEND_FILE         = 24;                    //追加文件
* STORAGE_PROTO_CMD_MODIFY_FILE         = 34;                    //修改追加文件
* STORAGE_PROTO_CMD_TRUNCATE_FILE       = 36;                    //删除追加文件
* STORAGE_PROTO_CMD_RESP	 = TRACKER_PROTO_CMD_RESP;
---
状态：
* FDFS_STORAGE_STATUS_INIT        = 0;
* FDFS_STORAGE_STATUS_WAIT_SYNC   = 1;
* FDFS_STORAGE_STATUS_SYNCING     = 2;
* FDFS_STORAGE_STATUS_IP_CHANGED  = 3;
* FDFS_STORAGE_STATUS_DELETED     = 4;
* FDFS_STORAGE_STATUS_OFFLINE     = 5;
* FDFS_STORAGE_STATUS_ONLINE      = 6;
* FDFS_STORAGE_STATUS_ACTIVE      = 7;
* FDFS_STORAGE_STATUS_NONE        = 99;

> 所有数据在传输过程中都是big-endian

---

##### header

位置 | 内容
---- | ------
0~7 | 包总长       
8  | 命令         
9  | 状态（0为正确）

##### 不带groupname的查询上传storage:

位置 | 内容
---- | ------
0~9  | header(TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE)
返回: |
0~9  | header(TRACKER_PROTO_CMD_RESP)
16~30 |IP地址
31~38 |PORT端口
39    |存储路径索引

##### 带groupname查询上传storage:

位置 | 内容
---- | ------
0~9   |header(TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE)
10~26 |group name
返回:|
0~9   |header(TRACKER_PROTO_CMD_RESP)
16~30 |IP地址
31~38 |PORT端口
39    |存储路径索引

##### upload:

位置 | 内容
---- | ------
0~9   | header
10    |

#### STORAGE_PROTO_CMD_DOWNLOAD_FILE 下载文件  

|位置|内容|
|----|----|
|0-9 |header(STORAGE_PROTO_CMD_DOWNLOAD_FILE)|
|10-17|offset|
|18-25|downloadfile length|
|26-31|group name|
|32-  |fileName|
|返回:|        |
|0-9  |header(STORAGE_PROTO_CMD_RESP)|

#### STORAGE_PROTO_CMD_UPLOAD_FILE 上传文件   
 
|位置|内容| 
|----|----|
|0-9 |header(STORAGE_PROTO_CMD_UPLOAD_FILE,body_len,0)|
|10  |storePathIndex(0)|
|11-18|file_size|
|19-24|FDFS_FILE_EXT_NAME_MAX_LEN|
|25-  |文件内容|
|返回：|       |
|0-7|pkg_len|
|8|STORAGE_PROTO_CMD_RESP|
|9|0|
|10-25|group_name|
|26-  |remote_filename|

body_len = 9 + 6 + file_size

#### STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE 上传从文件   
 
|位置|内容| 
|----|----|
|0-7 |body_len|
|8|STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE|
|9|0|
|10-17|主文件名长度master_filename.length()|
|18-25|文件大小file_size|
|26-41|FDFS_FILE_PREFIX_MAX_LEN|
|42-47|后缀名ext_name_bs|
|48-|主文件名master_filename|
|返回：|       |
|0-7|pkg_len|
|8|STORAGE_PROTO_CMD_RESP|
|9|0|
|10-25|group_name|
|26-  |remote_filename|

body_len = 16 + 16 + 6 +  master_filenameBytes.length + file_size

#### STORAGE_PROTO_CMD_SET_METADATA 设置meta  
|位置|内容|
|----|----|
|0-7|16+1+groupBytes.length+filenameBytes.length+meta_buff.length|
|8|STORAGE_PROTO_CMD_SET_METADATA|
|9|0|
|10-17|filenameBytes.length|
|18-25|meta_buff.length|
|26|STORAGE_SET_METADATA_FLAG_OVERWRITE|
|27-42|group_name|
|43-|file_name|
|-|meta_buff|
|返回:|
|0-7|pkg_len|
|8|STORAGE_PROTO_CMD_RESP|
|9|0|

#### STORAGE_PROTO_CMD_DELETE_FILE 删除文件  
 
|位置|内容| 
|----|----|
|0-7 |body_len|
|8|STORAGE_PROTO_CMD_DELETE_FILE|
|9|0|
|10-25|groupBytes|
|26-|filenameBytes|
|返回：|       |
|0-7|pkg_len|
|8|STORAGE_PROTO_CMD_RESP|
|9|0|



 

