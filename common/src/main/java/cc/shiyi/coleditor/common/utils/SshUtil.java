package cc.shiyi.coleditor.common.utils;

import com.google.common.base.Strings;
import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;

import java.io.*;

public final class SshUtil {

    public static final Integer DEFAULT_SSH_PORT = 22;

    public static ThreadLocal<ChannelShell> channelCache = ThreadLocal.withInitial(() -> null);

    public static String shell(Session session, String cmd) throws JSchException, IOException {
        ChannelShell channel = channelCache.get();
        // 修正通道有效性检查逻辑
        if (channel == null || !channel.isConnected()) {
            channel = (ChannelShell) session.openChannel("shell");
            channel.connect(100000);
            channelCache.set(channel);
        }

        // 优化输入输出流设置
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            channel.setInputStream(new ByteArrayInputStream(cmd.getBytes()));
            channel.setOutputStream(out);

            // 触发命令执行并刷新输出
            channel.getOutputStream().write('\n');
            channel.getOutputStream().flush();

            // 等待命令执行完成（替换错误的channel.w调用）
            //channel.waitForCondition(ChannelCondition.EXIT_STATUS, 0);

            // 读取完整输出内容
            return out.toString();
        }
    }

    public static String exec(Session session, String cmd) throws JSchException, IOException, InterruptedException {
        ChannelExec channel = (ChannelExec)session.openChannel("exec");
        channel.setCommand(cmd);

        ByteArrayOutputStream errOut = new ByteArrayOutputStream();
        channel.setErrStream(errOut);

        InputStream inputStream=channel.getInputStream();
        channel.connect(10000);
        channel.getOutputStream().flush();
        errOut.flush();

        InputStream out = IOUtils.toBufferedInputStream(inputStream);

        channel.disconnect();
        String outStr = IOUtils.toString(out, "UTF-8");
        String errStr = errOut.toString("UTF-8");

        if(!Strings.isNullOrEmpty(outStr)){
            return outStr;
        }
        if(!Strings.isNullOrEmpty(errStr)){
            return errStr;
        }
        return "未得到执行结果";
    }

    public static void upload(Session session, InputStream inputStream, String dst) throws SftpException, JSchException, IOException {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ((ChannelSftp)channel).put((inputStream), dst);
        ((ChannelSftp)channel).quit();
    }

    public static void download(Session session, String src, OutputStream outputStream) throws SftpException, JSchException, IOException {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ((ChannelSftp)channel).get(src, outputStream);
        ((ChannelSftp)channel).quit();
    }

    public static Session getSession(String host, String user, String passwd) throws Exception {
        return getSession(host, user, passwd, DEFAULT_SSH_PORT);
    }

    public static Session getSession(String host, String user, String passwd, Integer port) throws Exception {
        JSch jsch=new JSch();
        Session session=jsch.getSession(user, host, port);
        session.setPassword(passwd);

        session.setUserInfo(new UserInfo() {
            @Override
            public String getPassphrase() {
                return null;
            }

            @Override
            public String getPassword() {
                return passwd;
            }

            @Override
            public boolean promptPassword(String message) {
                return false;
            }

            @Override
            public boolean promptPassphrase(String message) {
                return false;
            }

            @Override
            public boolean promptYesNo(String message) {
                return true;
            }

            @Override
            public void showMessage(String message) {

            }
        });

        session.connect(300000);   // making a connection with timeout.
        return session;

    }



}
