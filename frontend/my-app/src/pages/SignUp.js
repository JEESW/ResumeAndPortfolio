import React, {useEffect, useState} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";

const SignUp = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [token, setToken] = useState("");
  const [email, setEmail] = useState("");
  const [emailSent, setEmailSent] = useState(false);
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [message, setMessage] = useState("");
  const [errors, setErrors] = useState({});

  // URL에 토큰이 있을 경우 → 자동으로 verify-token 호출
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const urlToken = params.get("token");
    if (urlToken) {
      axios
      .get(
          "https://www.jsw-resumeandportfolio.com/api/users/register/verify-token",
          {params: {token: urlToken}})
      .then((res) => {
        setToken(urlToken);
        setEmail(res.data.email);
        setStep(2);
      })
      .catch(() => {
        setMessage("이메일 인증 링크가 만료되었거나 유효하지 않습니다.");
      });
    }
  }, []);

  // 인증 이메일 처음 보내기
  const handleEmailInitiate = async () => {
    try {
      await axios.post(
          "https://www.jsw-resumeandportfolio.com/api/users/register/initiate",
          {
            email,
            password: "temporary123",
            confirmPassword: "temporary123",
            nickname: "temporaryUser",
          });
      setMessage("인증 이메일이 발송되었습니다. 이메일을 확인해주세요.");
      setEmailSent(true);
    } catch (err) {
      setMessage(err.response?.data?.message || "이메일 인증 요청 중 오류가 발생했습니다.");
    }
  };

  // 인증 메일 재전송
  const handleResendEmail = async () => {
    try {
      await axios.post(
          "https://www.jsw-resumeandportfolio.com/api/users/register/resend",
          null, {
            params: {email},
          });
      setMessage("새로운 인증 이메일이 발송되었습니다.");
    } catch (err) {
      setMessage(err.response?.data?.message || "재전송 중 오류가 발생했습니다.");
    }
  }

  // 유효성 검사 함수
  const validateInputs = () => {
    const newErrors = {};

    // 이메일
    if (!email) {
      newErrors.email = "이메일은 필수 항목입니다.";
    } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(
        email)) {
      newErrors.email = "올바른 이메일 형식이 아닙니다.";
    }

    // 비밀번호
    if (!password) {
      newErrors.password = "비밀번호는 필수 항목입니다.";
    } else if (password.length < 6) {
      newErrors.password = "비밀번호는 최소 6자 이상이어야 합니다.";
    }

    // 비밀번호 확인
    if (!confirmPassword) {
      newErrors.confirmPassword = "비밀번호 확인은 필수 항목입니다.";
    } else if (confirmPassword.length < 6) {
      newErrors.confirmPassword = "비밀번호는 최소 6자 이상이어야 합니다.";
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = "비밀번호가 일치하지 않습니다.";
    }

    // 닉네임
    if (!nickname) {
      newErrors.nickname = "닉네임은 필수 항목입니다.";
    } else if (nickname.length > 15) {
      newErrors.nickname = "닉네임은 최대 15자까지 가능합니다.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 회원가입 완료 요청
  const handleCompleteRegistration = async () => {
    if (!validateInputs()) {
      return;
    }

    try {
      await axios.post(
          "https://www.jsw-resumeandportfolio.com/api/users/register/complete",
          null, {
            params: {token, password, nickname},
          });
      setMessage("회원가입이 완료되었습니다! 로그인 페이지로 이동합니다...");
      setStep(3);
      setTimeout(() => navigate("/login"), 2000);
    } catch (err) {
      setMessage(err.response?.data?.message || "회원가입 중 오류가 발생했습니다.");
    }
  };

  return (
      <div
          className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
          <h2 className="text-2xl font-bold mb-4 text-gray-800">회원가입</h2>

          {message && <p className="text-sm mb-4 text-red-600">{message}</p>}

          {/* STEP 1: 이메일 입력 */}
          {step === 1 && (
              <>
                <label className="block text-gray-700 mb-1">이메일</label>
                <input
                    type="email"
                    className="w-full px-4 py-2 border rounded-lg"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="이메일을 입력하세요"
                />
                {!emailSent ? (
                    <button
                        onClick={handleEmailInitiate}
                        className="mt-2 w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700"
                    >
                      인증 이메일 보내기
                    </button>
                ) : (
                    <button
                        onClick={handleResendEmail}
                        className="mt-2 w-full bg-gray-300 text-gray-800 py-2 rounded-lg hover:bg-gray-400"
                    >
                      인증 메일 재전송
                    </button>
                )}
              </>
          )}

          {/* STEP 2: 비밀번호 + 닉네임 입력 */}
          {step === 2 && (
              <>
                <p className="text-gray-700 mb-4">이메일 인증이 완료되었습니다.</p>
                <label className="block text-gray-700 mb-1 mt-4">비밀번호</label>
                <input
                    type="password"
                    className="w-full px-4 py-2 border rounded-lg"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="비밀번호"
                />
                {errors.password && <p
                    className="text-red-600 text-sm mt-1">{errors.password}</p>}
                <label className="block text-gray-700 mb-1 mt-4">비밀번호 확인</label>
                <input
                    type="password"
                    className="w-full px-4 py-2 border rounded-lg"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="비밀번호 확인"
                />
                {errors.confirmPassword && <p
                    className="text-red-600 text-sm mt-1">{errors.confirmPassword}</p>}
                <label className="block text-gray-700 mb-1 mt-4">닉네임</label>
                <input
                    type="text"
                    className="w-full px-4 py-2 border rounded-lg"
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                    placeholder="닉네임"
                />
                {errors.nickname && <p
                    className="text-red-600 text-sm mt-1">{errors.nickname}</p>}
                <button
                    onClick={handleCompleteRegistration}
                    className="mt-4 w-full bg-green-600 text-white py-2 rounded-lg hover:bg-green-700"
                >
                  회원가입 완료
                </button>
              </>
          )}

          {/* STEP 3: 완료 메시지 */}
          {step === 3 && (
              <p className="text-green-700 font-semibold text-center">
                회원가입이 완료되었습니다! 로그인 페이지로 이동 중...
              </p>
          )}
        </div>
      </div>
  );
};

export default SignUp;