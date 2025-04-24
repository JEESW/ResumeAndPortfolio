import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      const response = await axios.post(
          "https://www.jsw-resumeandportfolio.com/api/users/login",
          {
            email,
            password,
          }
      );

      const accessToken = response.headers["authorization"];
      localStorage.setItem("accessToken", accessToken); // Access Token 저장
      alert("로그인 성공!");
      window.location.href = "/"; // 로그인 후 홈으로 이동
    } catch (err) {
      setError("로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.");
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = "https://www.jsw-resumeandportfolio.com/api/users/oauth2/authorization/google";
  };

  return (
      <div
          className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md">
          <h2 className="text-2xl font-bold mb-4 text-gray-800">로그인</h2>
          <p className="text-gray-600 mb-6">환영합니다!</p>
          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-gray-700">
                Email
              </label>
              <input
                  type="email"
                  id="email"
                  className="w-full px-4 py-2 border rounded-lg"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
              />
            </div>
            <div>
              <label htmlFor="password" className="block text-gray-700">
                Password
              </label>
              <input
                  type="password"
                  id="password"
                  className="w-full px-4 py-2 border rounded-lg"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
              />
              <div className="text-right mt-1">
                <span
                    className="text-sm text-blue-600 hover:underline cursor-pointer"
                    onClick={() => navigate("/reset-password")}
                >
                  Forgot your password?
                </span>
              </div>
            </div>
            {error && <p className="text-red-500 text-sm">{error}</p>}
            <button
                type="submit"
                className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition"
            >
              Sign in
            </button>
          </form>
          <div className="mt-6 text-center">
            <button
                onClick={handleGoogleLogin}
                className="block w-full bg-gray-200 py-2 rounded-lg shadow hover:bg-gray-300"
            >
              Sign in with Google
            </button>
            <p className="mt-4 text-gray-600 text-sm">
              Don't have an account?{" "}
              <span
                  className="text-blue-600 cursor-pointer"
                  onClick={() => navigate("/signup")}
              >
                Sign up now
              </span>
            </p>
          </div>
        </div>
      </div>
  );
};

export default Login;