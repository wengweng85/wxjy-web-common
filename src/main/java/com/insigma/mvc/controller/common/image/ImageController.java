package com.insigma.mvc.controller.common.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.insigma.mvc.MvcHelper;
import com.insigma.dto.AjaxReturnMsg;

@Controller
public class ImageController extends MvcHelper{

	private static Logger logger = Logger.getLogger(ImageController.class);
	private static int WIDTH = 100;
	private static int HEIGHT = 30;
	private static int NUM = 4;
	private static char[] seq = { '1' , '2', '3', '4', '5', '6', '7', '8', '9' };
	
	@RequestMapping(value="/image/create")
	protected ModelAndView handleRequestInternal(HttpServletRequest request,HttpServletResponse response) throws Exception {
		HttpSession session = request.getSession();
		// 禁止图片缓存
		response.setHeader("Pragma", "no-cache");  
        response.setHeader("Cache-Control", "no-cache");  
        response.setDateHeader("Expires", 0);
        
		// 设置响应类型
		response.setContentType("image/jpeg");
		Random r = new Random();

		// 图片的内存映像
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

		// 获得画笔对象
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.WHITE);

		// 用于存储随机生成的验证码
		StringBuffer number = new StringBuffer();

		// 绘制验证码
		for (int i = 0; i < NUM; i++) {
			g.setColor(Color.BLACK);
			//int h = (int) HEIGHT * 90 / 100;
			int h = 15+r.nextInt(10);
			g.setFont(new Font(null, Font.BOLD | Font.ITALIC, h));
			String ch = String.valueOf(seq[r.nextInt(seq.length)]);
			number.append(ch);
			g.drawString(ch, 10+i * WIDTH / NUM * 90 / 100, h);
		}
		
		session.setAttribute("yzm", number.toString().trim());

		// 绘制干扰线
		for (int i = 0; i <= 6; i++) {
			g.setColor(randomColor(r));
			g.drawLine(r.nextInt(WIDTH), r.nextInt(HEIGHT), r.nextInt(WIDTH), r
					.nextInt(HEIGHT));

		}
		g.dispose();
		ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
		// 压缩成jpeg格式
		ImageIO.write(image, "jpeg", jpegOutputStream);
		ServletOutputStream os = response.getOutputStream();
		os.write(jpegOutputStream.toByteArray());
		os.flush();
		os.close();

		return null;
	}
	
	private Color randomColor(Random r) {
		return new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
	}

	
	//验证验证码
	@RequestMapping(value="/image/check")
	@ResponseBody
	public AjaxReturnMsg toCheckYZM(HttpServletRequest request, HttpServletResponse response, HttpSession session,String aaa) throws Exception {
		try {
			String yzm = session.getAttribute("yzm").toString();
			session.removeAttribute("yzm");
			return yzm.equals(aaa) ? this.success("验证码正确") : this.error("验证码错误");
		} catch (Exception e) {
			e.printStackTrace();
			return this.error("校验失败原因：" + e);
		}	
	}
	
	
}
