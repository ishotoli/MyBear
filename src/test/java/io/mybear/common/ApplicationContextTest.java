package io.mybear.common;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationContextTest extends TestCase{
    private Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

    private ApplicationContext context;

    @Override
    protected void setUp() throws Exception {
        context = new ApplicationContext("tracker.conf");
        logger.debug("context: {}", context);
    }

    public void testGetProperty(){
        assertEquals(context.getProperty("disabled"), "false");
        assertEquals(context.getProperty("bind_addr"), "127.0.0.1");
        assertEquals(context.getIntValue("port", 0), 22122);
        assertEquals(context.getProperty("connect_timeout"), "30");
        assertEquals(context.getProperty("network_timeout"), "60");
        assertEquals(context.getProperty("accept_threads"), "1");
        assertEquals(context.getProperty("work_threads"), "4");
        assertEquals(context.getProperty("store_lookup"), "2");
        assertEquals(context.getProperty("store_group"), "group2");
        assertEquals(context.getProperty("store_server"), "0");
        assertEquals(context.getProperty("store_path"), "0");
        assertEquals(context.getProperty("download_server"), "0");
        assertEquals(context.getProperty("reserved_storage_space"), "10%");
        assertEquals(context.getProperty("log_level"), "info");
        assertEquals(context.getProperty("run_by_group"), "");
        assertEquals(context.getProperty("run_by_user"), "");
        assertEquals(context.getProperty("allow_hosts"), "*");
        assertEquals(context.getProperty("sync_log_buff_interval"), "10");
        assertEquals(context.getProperty("check_active_interval"), "120");
        assertEquals(context.getProperty("thread_stack_size"), "64KB");
        assertEquals(context.getProperty("storage_ip_changed_auto_adjust"), "true");
        assertEquals(context.getProperty("storage_sync_file_max_delay"), "86400");
        assertEquals(context.getProperty("storage_sync_file_max_time"), "300");
        assertEquals(context.getProperty("use_trunk_file"), "false");
        assertEquals(context.getProperty("slot_min_size"), "256");
        assertEquals(context.getProperty("slot_max_size"), "16MB");
        assertEquals(context.getProperty("trunk_file_size"), "64MB");
        assertEquals(context.getProperty("trunk_create_file_advance"), "false");
        assertEquals(context.getProperty("trunk_create_file_time_base"), "02:00");
        assertEquals(context.getProperty("trunk_create_file_interval"), "86400");
        assertEquals(context.getProperty("trunk_create_file_space_threshold"), "20G");
        assertEquals(context.getProperty("trunk_init_check_occupying"), "false");
        assertEquals(context.getProperty("trunk_init_reload_from_binlog"), "false");
        assertEquals(context.getProperty("trunk_compress_binlog_min_interval"), "0");
        assertEquals(context.getProperty("use_storage_id"), "false");
        assertEquals(context.getProperty("storage_ids_filename"), "storage_ids.conf");
        assertEquals(context.getProperty("id_type_in_filename"), "ip");
        assertEquals(context.getProperty("store_slave_file_use_link"), "false");
        assertEquals(context.getProperty("rotate_error_log"), "false");
        assertEquals(context.getProperty("error_log_rotate_time"), "00:00");
        assertEquals(context.getProperty("rotate_error_log_size"), "0");
        assertEquals(context.getProperty("log_file_keep_days"), "0");
        assertEquals(context.getProperty("use_connection_pool"), "false");
        assertEquals(context.getProperty("connection_pool_max_idle_time"), "3600");
        assertEquals(context.getProperty("http.server_port"), "8080");
        assertEquals(context.getProperty("http.check_alive_interval"), "30");
        assertEquals(context.getProperty("http.check_alive_type"), "tcp");
        assertEquals(context.getProperty("http.check_alive_uri"), "/status.html");
        assertEquals(context.getProperty("active_profile"), "dev");
    }
}
