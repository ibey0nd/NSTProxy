package burp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import com.mysql.jdbc.Connection;

import db.jdbcUtils;
import proxy.utils;

public class BurpExtender implements IBurpExtender,IScannerCheck,ITab {

	public IBurpExtenderCallbacks callbacks;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	private JPanel contentPanel;
	public jdbcUtils db;
	public Connection conn;
	protected JCheckBox proxyState;
	protected JTextField mysqlconnstr;
	
	@Override
	public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse) {
		//从UI面板上获取设置的参数
		conn = db.getConnection(mysqlconnstr.getText());
		if(!(proxyState.isSelected())) {
			return null;
		}
		
		
		String url = this.helpers.analyzeRequest(baseRequestResponse).getUrl().toString();
		if(utils.isblackext(url) || utils.isblackdomain(url)) {
			return null;
		}
		
		
		
		List<String> headers = this.helpers.analyzeRequest(baseRequestResponse).getHeaders();
		String allHeaders = "";
		for (String string : headers) {
			if (string.contains(":")) {
				allHeaders += string+"\r\n";
			}
			
		}
		
		List<IParameter> parameters = this.helpers.analyzeRequest(baseRequestResponse).getParameters();
		
		String allparam = utils.iparam2string(parameters);
		
		if((db.queryrepeat(url, allparam, conn))>0) {
			stdout.println("[-] repeat request ");
			return null;
		}
		
		
		Map<String,String> param = new HashMap<>();
		
		param.put("url", url);
		param.put("method", this.helpers.analyzeRequest(baseRequestResponse).getMethod());
		param.put("headers", allHeaders);
		param.put("body", allparam);
		
		db.insert(param,conn,stdout);
		
		return null;
	}

	@Override
	public List<IScanIssue> doActiveScan(IHttpRequestResponse baseRequestResponse,
			IScannerInsertionPoint insertionPoint) {
		return null;
	}

	@Override
	public int consolidateDuplicateIssues(IScanIssue existingIssue, IScanIssue newIssue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		this.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		stdout = new PrintWriter(callbacks.getStdout(),true);
		db = jdbcUtils.getInitJDBCUtil();
		
		bpGui(callbacks);
		callbacks.setExtensionName("NST Proxy");
		callbacks.registerScannerCheck(this);
		stdout.println("NST Proxy V1.1 load Success :  http://www.isnst.com \nAuthor: bey0nd");
		
	}

	private void bpGui(IBurpExtenderCallbacks callbacks2) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				contentPanel = new JPanel();
				contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				contentPanel.setLayout(new BorderLayout(0, 0));
				proxyState = new JCheckBox("Proxy    ");
				
				proxyState.setSelected(true);
				
				
				
				//----------------------------------
//				配置面板
				JPanel enableConfigPanel = new JPanel();
				enableConfigPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
				FlowLayout flowLayout = (FlowLayout) enableConfigPanel.getLayout();
				flowLayout.setAlignment(FlowLayout.LEFT);
//				配置面板中控件
				JPanel panel_3 = new JPanel();
				panel_3.setBorder(null);
				enableConfigPanel.add(panel_3);
				panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
				JLabel enableFor = new JLabel("Enable For :");
				panel_3.add(enableFor);
				panel_3.add(proxyState);
				//----------------------------------
				
//				域名白名单
				JPanel enableConfigPanel1 = new JPanel();
				enableConfigPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
				FlowLayout flowLayout1 = (FlowLayout) enableConfigPanel1.getLayout();
				flowLayout.setAlignment(FlowLayout.LEFT);
				
//				单独域名检测
				JPanel panel_5 = new JPanel();
				panel_5.setBorder(null);
				enableConfigPanel1.add(panel_5, BorderLayout.NORTH);
				panel_5.setLayout(new GridLayout(0, 1, 0, 0));
				JLabel mysql = new JLabel("MySQL Connect  : ");
				mysqlconnstr = new JTextField();
				mysqlconnstr.setText("jdbc:mysql://172.16.32.28:3306/scan?user=root&password=1&useUnicode=true&characterEncoding=UTF-8");;
				mysqlconnstr.setColumns(40);
				panel_5.add(mysql);
				panel_5.add(mysqlconnstr);
				
				contentPanel.add(enableConfigPanel, BorderLayout.NORTH);
				contentPanel.add(enableConfigPanel1, BorderLayout.WEST);
				callbacks.customizeUiComponent(contentPanel);
				callbacks.addSuiteTab(BurpExtender.this);
			}
		});
	}

	@Override
	public String getTabCaption() {
		// TODO Auto-generated method stub
		return "NSTProxy";
	}

	@Override
	public Component getUiComponent() {
		// TODO Auto-generated method stub
		return contentPanel;
	}

}
