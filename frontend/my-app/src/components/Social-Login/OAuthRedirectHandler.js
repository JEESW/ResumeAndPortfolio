import React, {useEffect} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import axios from "axios";

function OAuthRedirectHandler() {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const oneTimeCode = params.get("code");

    if (oneTimeCode) {
      // 백엔드로 oneTimeCode를 보내서 Access Token을 요청
      axios
      .post(
          `https://www.jsw-resumeandportfolio.com/api/users/oauth2/token?code=${oneTimeCode}`,
          {},
          {validateStatus: () => true}
      )
      .then((response) => {
        const authHeader = response.headers["authorization"];

        if (!authHeader || !authHeader.startsWith("Bearer ")) {
          throw new Error("토큰이 없거나 형식이 잘못됨");
        }

        const accessToken = authHeader.replace("Bearer ", "");
        localStorage.setItem("accessToken", accessToken);
        alert("로그인 성공!");
        window.location.href = "/";
      })
      .catch((error) => {
        console.error("Failed to get token:", error);
        alert("인증에 실패했습니다.");
      });
    }
  }, [location, navigate]);

  return <div>로그인 처리 중입니다...</div>;
}

export default OAuthRedirectHandler;