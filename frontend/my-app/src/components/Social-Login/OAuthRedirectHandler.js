import React, { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
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
          "https://www.jsw-resumeandportfolio.com/api/users/oauth2/token",
          null,
          {
            params: { code: oneTimeCode },
          }
      )
      .then((response) => {
        const accessToken = response.data.accessToken;
        localStorage.setItem("accessToken", accessToken);
        alert("로그인 성공!");
        navigate("/");
      })
      .catch((error) => {
        console.error("Failed to get token:", error);
        alert("인증에 실패했습니다.");
      });
    }
  }, [location, navigate]);

  return <div>Loading...</div>;
}

export default OAuthRedirectHandler;