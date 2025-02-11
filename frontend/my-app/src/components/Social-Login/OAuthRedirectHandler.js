// 예: React의 useEffect 훅을 사용해 처리
import { useEffect } from "react";
import { useLocation } from "react-router-dom";
import axios from "axios";

function OAuthRedirectHandler() {
  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const oneTimeCode = params.get("code");

    if (oneTimeCode) {
      // 백엔드로 oneTimeCode를 보내서 Access Token을 요청
      axios
      .post("https://api.example.com/api/users/oauth2/token", null, {
        params: { code: oneTimeCode }
      })
      .then(response => {
        const accessToken = response.data.accessToken;
        console.log("Access Token:", accessToken);
        // Access Token을 로컬 스토리지 또는 상태에 저장
      })
      .catch(error => {
        console.error("Failed to get token:", error);
      });
    }
  }, [location]);

  return <div>Loading...</div>;
}

export default OAuthRedirectHandler;