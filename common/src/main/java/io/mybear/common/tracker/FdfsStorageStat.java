package io.mybear.common.tracker;

import java.util.Date;

public class FdfsStorageStat {
    private long totalUploadCount;

    private long successUploadCount;

    private long totalAppendCount;

    private long successAppendCount;

    private long totalModifyCount;

    private long successModifyCount;

    private long totalTruncateCount;

    private long successTruncateCount;

    private long totalSetMetaCount;

    private long successSetMetaCount;

    private long totalDeleteCount;

    private long successDeleteCount;

    private long totalDownloadCount;

    private long successDownloadCount;

    private long totalGetMetaCount;

    private long successGetMetaCount;

    private long totalCreateLinkCount;

    private long successCreateLinkCount;

    private long totalDeleteLinkCount;

    private long successDeleteLinkCount;

    private long totalUploadBytes;

    private long successUploadBytes;

    private long totalAppendBytes;

    private long successAppendBytes;

    private long totalModifyBytes;

    private long successModifyBytes;

    private long totalDownloadBytes;

    private long successDownloadBytes;

    private long totalSyncInBytes;

    private long successSyncInBytes;

    private long totalSyncOutBytes;

    private long successSyncOutBytes;

    private long totalFileOpenCount;

    private long successFileOpenCount;

    private long totalFileReadCount;

    private long successFileReadCount;

    private long totalFileWriteCount;

    private long successFileWriteCount;

    /* last update timestamp as source server,
           current server' timestamp
    */
    private Date lastSourceUpdate = new Date(0l);

    /* last update timestamp as dest server,
           current server' timestamp
    */
    private Date lastSyncUpdate = new Date(0l);

    /* last syned timestamp,
       source server's timestamp
    */
    private Date lastSyncedTimestamp = new Date(0l);

    // last heart beat time
    private Date lastHeartBeatTime = new Date(0l);

    private Connection connection = new Connection();

    public long getTotalUploadCount() {
        return totalUploadCount;
    }

    public void setTotalUploadCount(long totalUploadCount) {
        this.totalUploadCount = totalUploadCount;
    }

    public long getSuccessUploadCount() {
        return successUploadCount;
    }

    public void setSuccessUploadCount(long successUploadCount) {
        this.successUploadCount = successUploadCount;
    }

    public long getTotalAppendCount() {
        return totalAppendCount;
    }

    public void setTotalAppendCount(long totalAppendCount) {
        this.totalAppendCount = totalAppendCount;
    }

    public long getSuccessAppendCount() {
        return successAppendCount;
    }

    public void setSuccessAppendCount(long successAppendCount) {
        this.successAppendCount = successAppendCount;
    }

    public long getTotalModifyCount() {
        return totalModifyCount;
    }

    public void setTotalModifyCount(long totalModifyCount) {
        this.totalModifyCount = totalModifyCount;
    }

    public long getSuccessModifyCount() {
        return successModifyCount;
    }

    public void setSuccessModifyCount(long successModifyCount) {
        this.successModifyCount = successModifyCount;
    }

    public long getTotalTruncateCount() {
        return totalTruncateCount;
    }

    public void setTotalTruncateCount(long totalTruncateCount) {
        this.totalTruncateCount = totalTruncateCount;
    }

    public long getSuccessTruncateCount() {
        return successTruncateCount;
    }

    public void setSuccessTruncateCount(long successTruncateCount) {
        this.successTruncateCount = successTruncateCount;
    }

    public long getTotalSetMetaCount() {
        return totalSetMetaCount;
    }

    public void setTotalSetMetaCount(long totalSetMetaCount) {
        this.totalSetMetaCount = totalSetMetaCount;
    }

    public long getSuccessSetMetaCount() {
        return successSetMetaCount;
    }

    public void setSuccessSetMetaCount(long successSetMetaCount) {
        this.successSetMetaCount = successSetMetaCount;
    }

    public long getTotalDeleteCount() {
        return totalDeleteCount;
    }

    public void setTotalDeleteCount(long totalDeleteCount) {
        this.totalDeleteCount = totalDeleteCount;
    }

    public long getSuccessDeleteCount() {
        return successDeleteCount;
    }

    public void setSuccessDeleteCount(long successDeleteCount) {
        this.successDeleteCount = successDeleteCount;
    }

    public long getTotalDownloadCount() {
        return totalDownloadCount;
    }

    public void setTotalDownloadCount(long totalDownloadCount) {
        this.totalDownloadCount = totalDownloadCount;
    }

    public long getSuccessDownloadCount() {
        return successDownloadCount;
    }

    public void setSuccessDownloadCount(long successDownloadCount) {
        this.successDownloadCount = successDownloadCount;
    }

    public long getTotalGetMetaCount() {
        return totalGetMetaCount;
    }

    public void setTotalGetMetaCount(long totalGetMetaCount) {
        this.totalGetMetaCount = totalGetMetaCount;
    }

    public long getSuccessGetMetaCount() {
        return successGetMetaCount;
    }

    public void setSuccessGetMetaCount(long successGetMetaCount) {
        this.successGetMetaCount = successGetMetaCount;
    }

    public long getTotalCreateLinkCount() {
        return totalCreateLinkCount;
    }

    public void setTotalCreateLinkCount(long totalCreateLinkCount) {
        this.totalCreateLinkCount = totalCreateLinkCount;
    }

    public long getSuccessCreateLinkCount() {
        return successCreateLinkCount;
    }

    public void setSuccessCreateLinkCount(long successCreateLinkCount) {
        this.successCreateLinkCount = successCreateLinkCount;
    }

    public long getTotalDeleteLinkCount() {
        return totalDeleteLinkCount;
    }

    public void setTotalDeleteLinkCount(long totalDeleteLinkCount) {
        this.totalDeleteLinkCount = totalDeleteLinkCount;
    }

    public long getSuccessDeleteLinkCount() {
        return successDeleteLinkCount;
    }

    public void setSuccessDeleteLinkCount(long successDeleteLinkCount) {
        this.successDeleteLinkCount = successDeleteLinkCount;
    }

    public long getTotalUploadBytes() {
        return totalUploadBytes;
    }

    public void setTotalUploadBytes(long totalUploadBytes) {
        this.totalUploadBytes = totalUploadBytes;
    }

    public long getSuccessUploadBytes() {
        return successUploadBytes;
    }

    public void setSuccessUploadBytes(long successUploadBytes) {
        this.successUploadBytes = successUploadBytes;
    }

    public long getTotalAppendBytes() {
        return totalAppendBytes;
    }

    public void setTotalAppendBytes(long totalAppendBytes) {
        this.totalAppendBytes = totalAppendBytes;
    }

    public long getSuccessAppendBytes() {
        return successAppendBytes;
    }

    public void setSuccessAppendBytes(long successAppendBytes) {
        this.successAppendBytes = successAppendBytes;
    }

    public long getTotalModifyBytes() {
        return totalModifyBytes;
    }

    public void setTotalModifyBytes(long totalModifyBytes) {
        this.totalModifyBytes = totalModifyBytes;
    }

    public long getSuccessModifyBytes() {
        return successModifyBytes;
    }

    public void setSuccessModifyBytes(long successModifyBytes) {
        this.successModifyBytes = successModifyBytes;
    }

    public long getTotalDownloadBytes() {
        return totalDownloadBytes;
    }

    public void setTotalDownloadBytes(long totalDownloadBytes) {
        this.totalDownloadBytes = totalDownloadBytes;
    }

    public long getSuccessDownloadBytes() {
        return successDownloadBytes;
    }

    public void setSuccessDownloadBytes(long successDownloadBytes) {
        this.successDownloadBytes = successDownloadBytes;
    }

    public long getTotalSyncInBytes() {
        return totalSyncInBytes;
    }

    public void setTotalSyncInBytes(long totalSyncInBytes) {
        this.totalSyncInBytes = totalSyncInBytes;
    }

    public long getSuccessSyncInBytes() {
        return successSyncInBytes;
    }

    public void setSuccessSyncInBytes(long successSyncInBytes) {
        this.successSyncInBytes = successSyncInBytes;
    }

    public long getTotalSyncOutBytes() {
        return totalSyncOutBytes;
    }

    public void setTotalSyncOutBytes(long totalSyncOutBytes) {
        this.totalSyncOutBytes = totalSyncOutBytes;
    }

    public long getSuccessSyncOutBytes() {
        return successSyncOutBytes;
    }

    public void setSuccessSyncOutBytes(long successSyncOutBytes) {
        this.successSyncOutBytes = successSyncOutBytes;
    }

    public long getTotalFileOpenCount() {
        return totalFileOpenCount;
    }

    public void setTotalFileOpenCount(long totalFileOpenCount) {
        this.totalFileOpenCount = totalFileOpenCount;
    }

    public long getSuccessFileOpenCount() {
        return successFileOpenCount;
    }

    public void setSuccessFileOpenCount(long successFileOpenCount) {
        this.successFileOpenCount = successFileOpenCount;
    }

    public long getTotalFileReadCount() {
        return totalFileReadCount;
    }

    public void setTotalFileReadCount(long totalFileReadCount) {
        this.totalFileReadCount = totalFileReadCount;
    }

    public long getSuccessFileReadCount() {
        return successFileReadCount;
    }

    public void setSuccessFileReadCount(long successFileReadCount) {
        this.successFileReadCount = successFileReadCount;
    }

    public long getTotalFileWriteCount() {
        return totalFileWriteCount;
    }

    public void setTotalFileWriteCount(long totalFileWriteCount) {
        this.totalFileWriteCount = totalFileWriteCount;
    }

    public long getSuccessFileWriteCount() {
        return successFileWriteCount;
    }

    public void setSuccessFileWriteCount(long successFileWriteCount) {
        this.successFileWriteCount = successFileWriteCount;
    }

    public Date getLastSourceUpdate() {
        return lastSourceUpdate;
    }

    public void setLastSourceUpdate(Date lastSourceUpdate) {
        this.lastSourceUpdate = lastSourceUpdate;
    }

    public Date getLastSyncUpdate() {
        return lastSyncUpdate;
    }

    public void setLastSyncUpdate(Date lastSyncUpdate) {
        this.lastSyncUpdate = lastSyncUpdate;
    }

    public Date getLastSyncedTimestamp() {
        return lastSyncedTimestamp;
    }

    public void setLastSyncedTimestamp(Date lastSyncedTimestamp) {
        this.lastSyncedTimestamp = lastSyncedTimestamp;
    }

    public Date getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    public void setLastHeartBeatTime(Date lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public class Connection {
        private int allocCount;

        private volatile int currentCount;

        private int maxCount;

        public int getAllocCount() {
            return allocCount;
        }

        public void setAllocCount(int allocCount) {
            this.allocCount = allocCount;
        }

        public int getCurrentCount() {
            return currentCount;
        }

        public void setCurrentCount(int currentCount) {
            this.currentCount = currentCount;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }
    }
}
