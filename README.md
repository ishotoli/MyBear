# MyBear
Java高仿FastDFS
#### 如何在IDE中开发
在src\test\resources\storage.conf这个文件中的base_path和store_path0写上本地路径
例如:


store_path0 = d:/fastdfs

base_path = d:/fastdfs


之后使用src\test\java\io\mybear中的测试类启动
#### 2017-04-08 协议解析
| 命令                                       | 值                                   | 内容                                       |
| ---------------------------------------- | ----------------------------------- | ---------------------------------------- |
| * TRACKER_PROTO_CMD_STORAGE_JOIN         | 81                                  |                                          |
| * FDFS_PROTO_CMD_QUIT                    | 82                                  | 关闭socket时的通知消息                           |
| * TRACKER_PROTO_CMD_STORAGE_BEAT         | 83                                  | storage发送的心跳                             |
| * TRACKER_PROTO_CMD_STORAGE_REPORT_DISK_USAGE | 84                                  | storage向tracker反馈磁盘使用情况                  |
| * TRACKER_PROTO_CMD_STORAGE_REPLICA_CHG  | 85                                  | repl new storage servers                 |
| * TRACKER_PROTO_CMD_STORAGE_SYNC_SRC_REQ | 86                                  | src storage require sync                 |
| * TRACKER_PROTO_CMD_STORAGE_SYNC_DEST_REQ | 87                                  | dest storage require sync                |
| * TRACKER_PROTO_CMD_STORAGE_SYNC_NOTIFY  | 88                                  | sync done notify                         |
| * TRACKER_PROTO_CMD_STORAGE_SYNC_REPORT  | 89                                  | report src last synced time as dest server |
| * TRACKER_PROTO_CMD_STORAGE_SYNC_DEST_QUERY | 79                                  | dest storage query sync src storage server |
| * TRACKER_PROTO_CMD_STORAGE_REPORT_IP_CHANGED | 78                                  | storage server report it's ip changed    |
| * TRACKER_PROTO_CMD_STORAGE_CHANGELOG_REQ | 77                                  | storage server request storage server's changelog |
| * TRACKER_PROTO_CMD_STORAGE_REPORT_STATUS | 76                                  | report specified storage server status   |
| * TRACKER_PROTO_CMD_STORAGE_PARAMETER_REQ | 75                                  | storage server request parameters        |
| * TRACKER_PROTO_CMD_STORAGE_REPORT_TRUNK_FREE | 74                                  | storage report trunk free space          |
| * TRACKER_PROTO_CMD_STORAGE_REPORT_TRUNK_FID | 73                                  | storage report current trunk file id     |
| * TRACKER_PROTO_CMD_STORAGE_FETCH_TRUNK_FID | 72                                  | storage get current trunk file id        |
| * TRACKER_PROTO_CMD_STORAGE_GET_STATUS   | 71                                  | get storage status from tracker          |
| * TRACKER_PROTO_CMD_STORAGE_GET_SERVER_ID | 70                                  | get storage server id from tracker       |
| * TRACKER_PROTO_CMD_STORAGE_FETCH_STORAGE_IDS | 69                                  | get all storage ids from tracker         |
| * TRACKER_PROTO_CMD_STORAGE_GET_GROUP_NAME   109 | get storage group name from tracker |                                          |

#define TRACKER_PROTO_CMD_TRACKER_GET_SYS_FILES_START    61  //start of tracker get system data files
#define TRACKER_PROTO_CMD_TRACKER_GET_SYS_FILES_END      62  //end of tracker get system data files
#define TRACKER_PROTO_CMD_TRACKER_GET_ONE_SYS_FILE       63  //tracker get a system data file
#define TRACKER_PROTO_CMD_TRACKER_GET_STATUS             64  //tracker get status of other tracker
#define TRACKER_PROTO_CMD_TRACKER_PING_LEADER            65  //tracker ping leader
#define TRACKER_PROTO_CMD_TRACKER_NOTIFY_NEXT_LEADER     66  //notify next leader to other trackers
#define TRACKER_PROTO_CMD_TRACKER_COMMIT_NEXT_LEADER     67  //commit next leader to other trackers
#define TRACKER_PROTO_CMD_TRACKER_NOTIFY_RESELECT_LEADER 68  //storage notify reselect leader when split-brain

#define TRACKER_PROTO_CMD_SERVER_LIST_ONE_GROUP			90
* TRACKER_PROTO_CMD_SERVER_LIST_ALL_GROUPS    |91                  |列举所有group
* TRACKER_PROTO_CMD_SERVER_LIST_STORAGE  |92                  |列举所有storage
* TRACKER_PROTO_CMD_SERVER_DELETE_STORAGE|93                  |从tracker移除指定storage
  #define TRACKER_PROTO_CMD_SERVER_SET_TRUNK_SERVER	94

* TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE|101 |不带groupname的查询上传storage
* TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ONE|102               |查询下载storage
* TRACKER_PROTO_CMD_SERVICE_QUERY_UPDATE|103                  |查询更新操作storage（删除文件或者设置meta）
* TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE|104    |带groupname查询上传storage
* TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ALL|105               |获取所有可以下载的storage
* TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ALL|106 |获取所有可以上传的storage，不指定groupname
* TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ALL|107    |获取所有可以上传的storage，指定groupname
  #define TRACKER_PROTO_CMD_SERVER_DELETE_GROUP		108


* TRACKER_PROTO_CMD_RESP|100                                  |服务器返回消息命令
* FDFS_PROTO_CMD_ACTIVE_TEST|111                              |心跳，注意返回也是resp

  #define STORAGE_PROTO_CMD_REPORT_SERVER_ID9  
* STORAGE_PROTO_CMD_UPLOAD_FILE |11                           |文件上传到storage
  * STORAGE_PROTO_CMD_DELETE_FILE= 12                           |从storage删除文件
  * STORAGE_PROTO_CMD_SET_METADATA|13                          |设置metadata
* STORAGE_PROTO_CMD_DOWNLOAD_FILE|14                          |从storage下载文件
  * STORAGE_PROTO_CMD_GET_METADATA|15                          |获取metadata
  #define STORAGE_PROTO_CMD_SYNC_CREATE_FILE16
  #define STORAGE_PROTO_CMD_SYNC_DELETE_FILE17
  #define STORAGE_PROTO_CMD_SYNC_UPDATE_FILE18
  #define STORAGE_PROTO_CMD_SYNC_CREATE_LINK19
  #define STORAGE_PROTO_CMD_CREATE_LINK	20
* STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE  |21                    |上传文件到slave
* STORAGE_PROTO_CMD_QUERY_FILE_INFO    |22                    |查询文件信息
* STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE= 23                    |上传一个可追加文件
* STORAGE_PROTO_CMD_APPEND_FILE        |24                    |追加文件
* STORAGE_PROTO_CMD_MODIFY_FILE        |34                    |修改追加文件
* STORAGE_PROTO_CMD_TRUNCATE_FILE      |36                    |删除追加文件
  * STORAGE_PROTO_CMD_RESP|TRACKER_PROTO_CMD_RESP
---
状态：
* FDFS_STORAGE_STATUS_INIT       |0
* FDFS_STORAGE_STATUS_WAIT_SYNC  |1
* FDFS_STORAGE_STATUS_SYNCING    |2
* FDFS_STORAGE_STATUS_IP_CHANGED |3
* FDFS_STORAGE_STATUS_DELETED    |4
* FDFS_STORAGE_STATUS_OFFLINE    |5
* FDFS_STORAGE_STATUS_ONLINE     |6
* FDFS_STORAGE_STATUS_ACTIVE     |7
* FDFS_STORAGE_STATUS_NONE       |99

> 所有数据在传输过程中都是big-endian

---

##### header

| 位置   | 内容       |
| ---- | -------- |
| 0~7  | 包总长      |
| 8    | 命令       |
| 9    | 状态（0为正确） |

##### 不带groupname的查询上传storage:

| 位置    | 内容                                       |
| ----- | ---------------------------------------- |
| 0~9   | header(TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE) |
| 返回:   |                                          |
| 0~9   | header(TRACKER_PROTO_CMD_RESP)           |
| 16~30 | IP地址                                     |
| 31~38 | PORT端口                                   |
| 39    | 存储路径索引                                   |

##### 带groupname查询上传storage:

| 位置    | 内容                                       |
| ----- | ---------------------------------------- |
| 0~9   | header(TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE) |
| 10~26 | group name                               |
| 返回:   |                                          |
| 0~9   | header(TRACKER_PROTO_CMD_RESP)           |
| 10~25 | groupname                                |
| 26~46 | IP地址                                     |
| 47~48 | PORT端口                                   |
| 49    | 存储路径索引                                   |

#### STORAGE_PROTO_CMD_DOWNLOAD_FILE 下载文件  

| 位置    | 内容                                      |
| ----- | --------------------------------------- |
| 0-9   | header(STORAGE_PROTO_CMD_DOWNLOAD_FILE) |
| 10-17 | offset                                  |
| 18-25 | downloadfile length                     |
| 26-31 | group name                              |
| 32-   | fileName                                |
| 返回:   |                                         |
| 0-9   | header(STORAGE_PROTO_CMD_RESP)          |

#### STORAGE_PROTO_CMD_UPLOAD_FILE 上传文件   

| 位置    | 内容                                       |
| ----- | ---------------------------------------- |
| 0-9   | header(STORAGE_PROTO_CMD_UPLOAD_FILE,body_len,0) |
| 10    | storePathIndex(0)                        |
| 11-18 | file_size                                |
| 19-24 | FDFS_FILE_EXT_NAME_MAX_LEN               |
| 25-   | 文件内容                                     |
| 返回：   |                                          |
| 0-7   | pkg_len                                  |
| 8     | STORAGE_PROTO_CMD_RESP                   |
| 9     | 0                                        |
| 10-25 | group_name                               |
| 26-   | remote_filename                          |

body_len = 9 + 6 + file_size

#### STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE 上传从文件   

| 位置    | 内容                                  |
| ----- | ----------------------------------- |
| 0-7   | body_len                            |
| 8     | STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE |
| 9     | 0                                   |
| 10-17 | 主文件名长度master_filename.length()      |
| 18-25 | 文件大小file_size                       |
| 26-41 | FDFS_FILE_PREFIX_MAX_LEN            |
| 42-47 | 后缀名ext_name_bs                      |
| 48-   | 主文件名master_filename                 |
| 返回：   |                                     |
| 0-7   | pkg_len                             |
| 8     | STORAGE_PROTO_CMD_RESP              |
| 9     | 0                                   |
| 10-25 | group_name                          |
| 26-   | remote_filename                     |

body_len = 16 + 16 + 6 +  master_filenameBytes.length + file_size

#### STORAGE_PROTO_CMD_SET_METADATA 设置meta  
| 位置    | 内容                                       |
| ----- | ---------------------------------------- |
| 0-7   | 16+1+groupBytes.length+filenameBytes.length+meta_buff.length |
| 8     | STORAGE_PROTO_CMD_SET_METADATA           |
| 9     | 0                                        |
| 10-17 | filenameBytes.length                     |
| 18-25 | meta_buff.length                         |
| 26    | STORAGE_SET_METADATA_FLAG_OVERWRITE      |
| 27-42 | group_name                               |
| 43-   | file_name                                |
| -     | meta_buff                                |
| 返回:   |                                          |
| 0-7   | pkg_len                                  |
| 8     | STORAGE_PROTO_CMD_RESP                   |
| 9     | 0                                        |

#### STORAGE_PROTO_CMD_DELETE_FILE 删除文件  

| 位置    | 内容                            |
| ----- | ----------------------------- |
| 0-7   | body_len                      |
| 8     | STORAGE_PROTO_CMD_DELETE_FILE |
| 9     | 0                             |
| 10-25 | groupBytes                    |
| 26-   | filenameBytes                 |
| 返回：   |                               |
| 0-7   | pkg_len                       |
| 8     | STORAGE_PROTO_CMD_RESP        |
| 9     | 0                             |
