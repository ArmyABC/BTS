package com.example.board.controller;

import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.board.model.member.LoginForm;
import com.example.board.model.member.Member;
import com.example.board.model.member.MemberJoinForm;
import com.example.board.repository.MemberMapper;
import com.example.board.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("member")
@Controller
public class MemberController {
	
	// 데이터베이스 접근을 위한 MemberMapper 필드 선언
	private MemberService memberService;
	
	// MemberMapper 필드 객체 주입(setter 를 이용한 주입)
	@Autowired
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	
	//회원가입 페이지 이동
	@GetMapping("join")
	public String joinForm(Model model) {
		log.info("회원가입 페이지");
		// joinForm.html 의 필드 세팅을 위해 model 에 빈 MemberJoinForm 객체 생성하여 저장한다
		model.addAttribute("joinForm", new MemberJoinForm());
		// member/joinForm.html 페이지를 리턴한다.
		return "member/joinForm";
	}
	
	//회원가입
	//@Transactional(readOnly=true) //에러가 터지면 DB 컴파일 안하도록. BindingResult를 쓰기 때문에 없어도 무방.
	@PostMapping("join")
	public String join(@Validated @ModelAttribute("joinForm") MemberJoinForm joinForm,
						BindingResult result) {
		//회원가입 처리
		log.info("joinForm:{}", joinForm);
		log.info("result:{}", result);
		
		// validation 에 에러가 있으면 가입시키지 않고 member/joinForm.html 페이지로 돌아간다.
		if(result.hasErrors()) {
			return "/member/joinForm";
		}
		
		// 이메일 주소에 '@' 문자가 포함되어 있는지 확인한다.
    if (!joinForm.getEmail().contains("@")) {
        // BindingResult 객체에 GlobalError 를 추가한다.
        result.reject("emailError", "이메일 형식이 잘못되었습니다.");
        // member/joinForm.html 페이지를 리턴한다.
        return "member/joinForm";
    }
    // 사용자로부터 입력받은 아이디로 데이터베이스에서 Member 를 검색한다.
    Member member = memberService.findMember(joinForm.getMember_id());
    // 사용자 정보가 존재하면
    if (member != null) {
        log.info("이미 가입된 아이디 입니다.");
        // BindingResult 객체에 GlobalError 를 추가한다.
        result.reject("duplicate ID", "이미 가입된 아이디 입니다.");
        // member/joinForm.html 페이지를 리턴한다.
        return "member/joinForm";
    }
    // MemberJoinForm 객체를 Member 타입으로 변환하여 데이터베이스에 저장한다.
    memberService.saveMember(MemberJoinForm.toMember(joinForm));
    // 메인 페이지로 리다이렉트한다.
    return "redirect:/";
	}
	
	// 로그인 페이지 이동
  @GetMapping("login")
  public String loginForm(Model model) {
  		log.info("로그인 페이지");
      // member/loginForm.html 에 필드 셋팅을 위해 빈 LoginForm 객체를 생성하여 model 에 저장한다.
      model.addAttribute("loginForm", new LoginForm());
      // member/loginForm.html 페이지를 리턴한다.
      return "member/loginForm";
  }
	
  // 로그인 처리
	@PostMapping("login")
	public String login(@Validated @ModelAttribute("loginForm") LoginForm loginForm,
						BindingResult result,
						HttpServletResponse response,
						HttpServletRequest request,
						@RequestParam(defaultValue="/") String redirectURL) {
		log.info("loginForm: {}", loginForm);
		log.info("redirectURL : {}", redirectURL);
		// validation 에 실패하면 member/loginForm 페이지로 돌아간다.
		if(result.hasErrors()) {
			return "member/loginForm";
		}
		// 사용자가 입력한 이이디에 해당하는 Member 정보를 데이터베이스에서 가져온다.
		Member member = memberService.findMember(loginForm.getMember_id());
    // Member 가 존재하지 않거나 패스워드가 다르면
    if (member == null || !member.getPassword().equals(loginForm.getPassword())) {
        // BindingResult 객체에 GlobalError 를 발생시킨다.
        result.reject("loginError", "아이디가 없거나 패스워드가 다릅니다.");
        // member/loginForm.html 페이지로 돌아간다.
        return "member/loginForm";
    }
		
		//쿠키를 이용한 로그인처리
		//쿠키: 웹브라우저와 서버의 도메인 사이에 생성된 데이터로 클라이언트 사이드에 저장
        //Cookie cookie = new Cookie("cookieLoginId",findMember.getMember_id());
		//쿠키는 도메인의 디렉토리별로 저장되기 때문에 path를 /로 지정하여 모든 경로에서 읽을 수 있게 처리
        //cookie.setPath("/");
        //response.addCookie(cookie);
		
		//세션을 이용한 로그인처리
		//세션: 웹브라우저와 서버 사이에 생성된 데이터로 서버 사이드에 저장
        
    // Request 객체에서 Session 객체를 꺼내온다.
		HttpSession session = request.getSession();
		// Session 에 'loginMember' 라는 이름으로 Member 객체를 저장한다.
		session.setAttribute("loginMember", member);
		// 메인 페이지로 리다이렉트 한다.
		log.info("로그인 실행");
	 	return "redirect:" + redirectURL;
	}
	
	@GetMapping("sessionInfo")
	public String sessionInfo(HttpServletRequest request) {
		//getSession()는 비워놓으면 세션을 만듭니다. false:세션이 없으면 null 리턴
		HttpSession session = request.getSession(false);
		if(session == null) {
			return "redirect:/member/login";
		}
		log.info("sessionId:{}", session.getId());
		log.info("maxInactiveInterval:{}", session.getMaxInactiveInterval());
		log.info("creationTime:{}", new Date(session.getCreationTime()));
		log.info("lastAccessedTime:{}", new Date(session.getLastAccessedTime()));
		return "redirect:/";
	}
	
	@GetMapping("logout")
	public String logout(HttpServletResponse response,
						  HttpServletRequest request) {
		//전과 같은 이름을 만들면서 값을 null로
		//Cookie cookie = new Cookie("cookieLoginId", null);
		//cookie.setPath("/");
		//cookie.setMaxAge(0); //쿠키 유지시간
		//response.addCookie(cookie);
		
		//세션초기화 첫번째 방법
		//HttpSession session = request.getSession(false);
		//session.setAttribute("loginMember", null);
		//세션초기화 두번째 방법
		//session.invalidate();
		
		// Request 객체에서 Session 정보를 가져온다.
    HttpSession session = request.getSession(false);
    // 세션이 존재하면 세션의 모든 데이터를 리셋한다.
    if (session != null) {
        session.invalidate();
    }
    // 메인 페이지로 리다이렉트 한다.
    return "redirect:/";
	}
	
	
	
	
	
	
	
}
