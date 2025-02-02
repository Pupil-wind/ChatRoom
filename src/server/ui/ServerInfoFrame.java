package server.ui;

import common.model.entity.User;
import server.DataBuffer;
import server.controller.RequestProcessor;
import server.model.service.UserService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

//服务信息窗口
public class ServerInfoFrame extends JFrame {
    private static final long serialVersionUID = 6274443611957724780L;
    private JTextField jta_msg;
    private JTable onlineUserTable ;
    private JTable registedUserTable ;

    public ServerInfoFrame() {
        init();
        loadData();
        setVisible(true);
    }

    //初始化窗体
    public void init() {

        this.setIconImage(new ImageIcon("images/avatar.png").getImage());
        //设置服务器控制台标题
        this.setTitle("服务器控制台");
        this.setBounds((DataBuffer.screenSize.width - 700)/2,
                (DataBuffer.screenSize.height - 475)/2, 700, 475);
        this.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        panel.setBorder(BorderFactory.createTitledBorder(border,
                "服务器监控", TitledBorder.LEFT,TitledBorder.TOP));
        this.add(panel, BorderLayout.NORTH);

        JLabel label = new JLabel("服务器端口: ");
        panel.add(label);
        //关闭关闭服务器按钮
        JButton exitBtn = new JButton("关闭服务器");
        panel.add(exitBtn);

        JLabel la_msg = new JLabel("要发送的消息");
        panel.add(la_msg);
        // 服务器要发送消息的输入框
        jta_msg = new JTextField(30);
        // 定义一个监听器对 象：发送广播消息
        ActionListener sendCaseMsgAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sendAllMsg();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        };

        // 给输入框加航事件监听器，按回车时发送
        jta_msg.addActionListener(sendCaseMsgAction);
        JButton bu_send = new JButton("Send");
        // 给按钮加上发送广播消息的监听器
        bu_send.addActionListener(sendCaseMsgAction);
        panel.add(jta_msg);
        panel.add(bu_send);

        //使用服务器缓存中的TableModel
        onlineUserTable = new JTable(DataBuffer.onlineUserTableModel);
        registedUserTable = new JTable(DataBuffer.registedUserTableModel);

        // 取得表格上的弹出菜单对象,加到表格上
        JPopupMenu pop = getTablePop();
        onlineUserTable.setComponentPopupMenu(pop);

        //选项卡
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("在线用户列表", new JScrollPane(onlineUserTable));
        tabbedPane.addTab("已注册用户列表", new JScrollPane(registedUserTable));
        tabbedPane.setTabComponentAt(0, new JLabel("在线用户列表"));
        this.add(tabbedPane, BorderLayout.CENTER);

        final JLabel stateBar = new JLabel("", SwingConstants.RIGHT);
        stateBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //用定时任务来显示当前时间
        new java.util.Timer().scheduleAtFixedRate(
                new TimerTask(){
                    DateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                    public void run() {
                        stateBar.setText("当前时间：" + df.format(new Date()) + "  ");
                    }
                }, 0, 1000);
        //把状态栏添加到窗体的南边
        this.add(stateBar, BorderLayout.SOUTH);

        //关闭窗口
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        //添加关闭服务器按钮事件处理方法
        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                logout();
            }
        });
    }

    //创建表格上的弹出菜单对象，实现发信，踢人功能
    private JPopupMenu getTablePop() {
        //弹出菜单对象
        JPopupMenu pop = new JPopupMenu();
        JMenuItem mi_send = new JMenuItem("发信");
        //菜单项对象
        //设定菜单命令关键字
        mi_send.setActionCommand("send");
        //菜单项对象
        JMenuItem mi_del = new JMenuItem("踢掉");
        //设定菜单命令关键字
        mi_del.setActionCommand("del");
        //弹出菜单上的事件监听器对象
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = e.getActionCommand();
                //根据点击的菜单项，设定ActionCommand
                popMenuAction(s);
            }
        };
        mi_send.addActionListener(al);
        //给菜单加上监听器
        mi_del.addActionListener(al);
        pop.add(mi_send);
        pop.add(mi_del);
        return pop;
    }

    //处理弹出菜单上的事件
    private void popMenuAction(String command) {
        //得到在表格上选中的行
        final int selectIndex = onlineUserTable.getSelectedRow();
        String usr_id = (String)onlineUserTable.getValueAt(selectIndex,0);
        System.out.println("与用户: "+usr_id+"进行系统通信:");
        if (selectIndex == -1) {
            JOptionPane.showMessageDialog(this, "请选中一个用户");
            return;
        }

        if (command.equals("del")) {
            //从线程中移除处理线程对象
            try {
                RequestProcessor.remove(DataBuffer.onlineUsersMap.get(Long.valueOf(usr_id)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (command.equals("send")) {
            //发送对话框
            final JDialog jd = new JDialog(this, true);
            jd.setLayout(new FlowLayout());
            //对话框设置在屏幕中心, 长380高100
            jd.setBounds((DataBuffer.screenSize.width - 387)/2,
                    (DataBuffer.screenSize.height - 267)/2,
                    387, 100);
			jd.setTitle("发送系统消息");
            final JTextField jtd_m = new JTextField(20);
            JButton jb = new JButton("发送!");
            jd.add(jtd_m);
            jd.add(jb);
            //发送按钮的事件实现
            jb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String msg = jtd_m.getText();
                    try {
                        RequestProcessor.chat_sys(msg,DataBuffer.onlineUsersMap.get(Long.valueOf(usr_id)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    //清空输入框
                    jtd_m.setText("");
                    jd.dispose();
                    System.out.println("系统消息发送完成...");
                }
            });
            jd.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "未知菜单:" + command);
        }
        //刷新表格
        SwingUtilities.updateComponentTreeUI(onlineUserTable);
    }

    //按下发送服务器消息的按钮，给所有在线用户发送消息
    private void sendAllMsg() throws IOException {
        RequestProcessor.board(jta_msg.getText());
        //清空输入框
        jta_msg.setText("");
    }

    //把所有已注册的用户信息加载到RegistedUserTableModel中
    private void loadData(){
        List<User> users = new UserService().loadAllUser();
        for (User user : users) {
            DataBuffer.registedUserTableModel.add(new String[]{
                    String.valueOf(user.getId()),
                    user.getPassword(),
                    user.getNickname(),
                    String.valueOf(user.getSex())
            });
        }
    }

    //关闭服务器
    private void logout() {
        int select = JOptionPane.showConfirmDialog(ServerInfoFrame.this,
                "确定关闭吗？\n\n关闭服务器将中断与所有客户端的连接!",
                "关闭服务器",
                JOptionPane.YES_NO_OPTION);
        //如果用户点击的是关闭服务器按钮时会提示是否确认关闭。
        if (select == JOptionPane.YES_OPTION) {
            System.exit(0);//退出系统
        }else{
            //覆盖默认的窗口关闭事件动作
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }
}
