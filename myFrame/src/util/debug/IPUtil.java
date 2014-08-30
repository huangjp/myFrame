/*
 * 文件名：IPUtil.java	 
 * 时     间：下午7:13:44
 * 作     者：huangjp       
 * 版     权： 2014-2022  智慧园区, 公司保留所有权利.
 * 联     系：www.szyungu.com
 */
package util.debug;

import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @ClassName: IPUtil
 * @Description: (这里用一句话描述这个类的作用)
 * @author huangjp
 * @date 2014年4月1日 下午7:13:44
 */
public class IPUtil {
	JFrame frame;
	JLabel label1, label2;
	JPanel panel;
	String ip, address;

	public void getip() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress().toString();// 获得本机IP
			address = addr.getHostName().toString();// 获得本机名称

			// System.out.println("addr=:"+String.valueOf(addr));
		} catch (Exception e) {
			System.out.println("Bad IP Address!" + e);
		}
	}

	public void showframe() {
		frame = new JFrame("my ip");
		label1 = new JLabel("this my ip");
		label1.setText(ip);
		label2 = new JLabel("this my address");
		label2.setText(address);
		panel = new JPanel();
		panel.add(label1);
		panel.add(label2);
		frame.getContentPane().add(panel);

		frame.setSize(400, 300);
		frame.setVisible(true);
	}

	public static void main(String agrs[]) {
		IPUtil myip = new IPUtil();
		myip.getip();
		myip.showframe();

	}
}