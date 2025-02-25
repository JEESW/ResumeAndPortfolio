import React, {useState, useEffect} from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import Modal from "../Modal/Modal";

const Header = () => {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [nickname, setNickname] = useState("");

  // 로그인 상태 확인
  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      validateToken(token); // Access Token 검증 및 필요 시 재발급
    }
  }, [isLoggedIn]); // isLoggedIn이 변경될 때마다 실행됨

  // Access Token 검증 및 재발급 처리
  const validateToken = async (token) => {
    try {
      await fetchUserInfo(token); // 사용자 정보 조회
    } catch (error) {
      if (error.response && error.response.status === 401) {
        // Access Token 재발급
        try {
          const reissueResponse = await axios.post(
              "https://www.jsw-resumeandportfolio.com/api/users/reissue"
          );
          let newAccessToken = reissueResponse.headers["authorization"];

          if (newAccessToken && !newAccessToken.startsWith("Bearer ")) {
            newAccessToken = `Bearer ${newAccessToken}`;
          }

          localStorage.setItem("accessToken", newAccessToken); // 새로운 Access Token 저장
          await fetchUserInfo(newAccessToken); // 새로운 토큰으로 사용자 정보 재조회
        } catch (reissueError) {
          console.error("Token reissue failed:", reissueError);
          localStorage.removeItem("accessToken");
          alert("로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
          navigate("/login");
        }
      }
    }
  };

  // 사용자 정보 조회
  const fetchUserInfo = async (token) => {
    let formattedToken = token.startsWith("Bearer ") ? token.split(" ")[1]
        : token;

    const response = await axios.get(
        "https://www.jsw-resumeandportfolio.com/api/users/me",
        {
          headers: {
            Authorization: `Bearer ${formattedToken}`, // Access Token을 헤더에 추가
          },
        }
    );
    setNickname(response.data.nickname); // 닉네임 설정
    setIsLoggedIn(true);
  };

  // 로그아웃 핸들러
  const handleLogout = async () => {
    try {
      const token = localStorage.getItem("accessToken");

      if (!token) {
        console.error("No access token found");
        alert("로그인 상태가 아닙니다.");
        return;
      }

      await axios.post(
          "https://www.jsw-resumeandportfolio.com/api/users/logout",
          {},
          {
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
          }
      );

      localStorage.removeItem("accessToken"); // 토큰 삭제
      setIsLoggedIn(false);
      navigate("/");
    } catch (err) {
      console.error("Logout failed:", err);
      alert("로그아웃 중 문제가 발생했습니다.");
    }
  };

  return (
      <>
        <header className="bg-white shadow-md">
          <div
              className="container mx-auto px-4 py-3 flex justify-between items-center"
          >
            {/* 로고 */}
            <div className="text-lg font-bold text-blue-600">
              <a href="/" className="text-blue-600">
                Resume&Portfolio
              </a>
            </div>

            {/* 네비게이션 */}
            <nav className="hidden md:flex space-x-6">
              <a href="/" className="text-gray-700 hover:text-blue-600">
                Home
              </a>
              <a href="/resume" className="text-gray-700 hover:text-blue-600">
                Resume
              </a>
              <a href="/portfolio"
                 className="text-gray-700 hover:text-blue-600">
                Portfolio
              </a>
              <a href="/faq" className="text-gray-700 hover:text-blue-600">
                FAQ
              </a>
            </nav>

            {/* 로그인/로그아웃 및 버튼 */}
            <div className="flex items-center space-x-4">
              {!isLoggedIn ? (
                  <>
                    {/* 로그인 버튼 */}
                    <button
                        onClick={() => navigate("/login")}
                        className="text-gray-700 hover:text-blue-600"
                    >
                      Login
                    </button>
                    {/* 회원가입 버튼 */}
                    <button
                        onClick={() => navigate("/signup")}
                        className="bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
                    >
                      Sign Up
                    </button>
                  </>
              ) : (
                  <>
                    {/* 로그아웃 버튼 */}
                    <button
                        onClick={handleLogout}
                        className="text-gray-700 hover:text-blue-600"
                    >
                      Logout
                    </button>
                    {/* 마이페이지 버튼 */}
                    <button
                        onClick={() => setIsModalOpen(true)}
                        className="bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
                    >
                      My Page
                    </button>
                  </>
              )}
            </div>

            {/* 모바일 메뉴 */}
            <div className="md:hidden">
              <button className="text-gray-700">
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-6 w-6"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    strokeWidth={2}
                >
                  <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M4 6h16M4 12h16m-7 6h7"
                  />
                </svg>
              </button>
            </div>
          </div>
        </header>

        {/* 모달 */}
        <Modal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            nickname={nickname}
        />
      </>
  );
};

export default Header;