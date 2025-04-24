import React, {useEffect, useState} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";

const ResetPassword = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [email, setEmail] = useState("");
  const [token, setToken] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");
  const [errors, setErrors] = useState({});

  // URL에 토큰이 있을 경우 자동으로 토큰 유효성 검증
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const urlToken = params.get("token");
    if (urlToken) {
      axios
      .get(
          "https://www.jsw-resumeandportfolio.com/api/users/reset-password/verify-token",
          {
            params: {token: urlToken},
          })
      .then((res) => {
        setToken(urlToken);
        setEmail(res.data.email);
        setStep(2);
      })
      .catch(() => {
        setMessage("비밀번호 재설정 링크가 만료되었거나 유효하지 않습니다.");
      });
    }
  }, []);

  // 이메일로 인증 요청
  const handleEmailSubmit = async () => {
    try {
      await axios.post(
          "https://www.jsw-resumeandportfolio.com/api/users/reset-password/request",
          {
            email,
          });
      setMessage("비밀번호 재설정 이메일이 발송되었습니다. 이메일을 확인하세요.");
    } catch (err) {
      setMessage(err.response?.data?.message || "이메일 전송 중 오류가 발생했습니다.");
    }
  };

  // 유효성 검사
  const validatePassword = () => {
    const newErrors = {};
    if (!password) {
      newErrors.password = "비밀번호를 입력해주세요.";
    } else if (password.length < 6) {
      newErrors.password = "비밀번호는 최소 6자 이상이어야 합니다.";
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = "비밀번호 확인을 입력해주세요.";
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = "비밀번호가 일치하지 않습니다.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 비밀번호 재설정 요청
  const handlePasswordReset = async () => {
    if (!validatePassword()) {
      return;
    }

    try {
      await axios.post(
          "https://www.jsw-resumeandportfolio.com/api/users/reset-password/confirm",
          {
            token,
            newPassword: password,
          });
      setMessage("비밀번호가 성공적으로 변경되었습니다. 로그인 페이지로 이동합니다...");
      setStep(3);
      setTimeout(() => navigate("/login"), 2000);
    } catch (err) {
      setMessage(err.response?.data?.message || "비밀번호 재설정 중 오류가 발생했습니다.");
    }
  };

  return (
      <div
          className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
          <h2 className="text-2xl font-bold mb-4 text-gray-800">비밀번호 재설정</h2>

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
                    placeholder="가입한 이메일을 입력하세요"
                />
                <button
                    onClick={handleEmailSubmit}
                    className="mt-2 w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700"
                >
                  인증 메일 보내기
                </button>
              </>
          )}

          {/* STEP 2: 비밀번호 입력 */}
          {step === 2 && (
              <>
                <p className="text-gray-700 mb-4">이메일 인증이 완료되었습니다. 새 비밀번호를
                  설정하세요.</p>

                <label className="block text-gray-700 mb-1">새 비밀번호</label>
                <input
                    type="password"
                    className="w-full px-4 py-2 border rounded-lg"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="새 비밀번호"
                />
                {errors.password && <p
                    className="text-red-600 text-sm">{errors.password}</p>}

                <label className="block text-gray-700 mb-1 mt-4">비밀번호 확인</label>
                <input
                    type="password"
                    className="w-full px-4 py-2 border rounded-lg"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="비밀번호 확인"
                />
                {errors.confirmPassword && <p
                    className="text-red-600 text-sm">{errors.confirmPassword}</p>}

                <button
                    onClick={handlePasswordReset}
                    className="mt-4 w-full bg-green-600 text-white py-2 rounded-lg hover:bg-green-700"
                >
                  비밀번호 재설정 완료
                </button>
              </>
          )}

          {/* STEP 3: 완료 메시지 */}
          {step === 3 && (
              <p className="text-green-700 font-semibold text-center">
                비밀번호가 변경되었습니다. 로그인 페이지로 이동 중...
              </p>
          )}
        </div>
      </div>
  );
};

export default ResetPassword;