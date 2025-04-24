import React, {useState, useEffect} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";

const UpdateProfile = () => {
  const navigate = useNavigate();
  const [nickname, setNickname] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [message, setMessage] = useState("");
  const [errors, setErrors] = useState({});

  useEffect(() => {
    axios.get("https://www.jsw-resumeandportfolio.com/api/users/me", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
      }
    }).then((res) => {
      setNickname(res.data.nickname);
    }).catch(() => {
      setMessage("사용자 정보를 불러오는 데 실패했습니다.");
    });
  }, []);

  // 유효성 검사 함수
  const validateInputs = () => {
    const newErrors = {};

    if (!currentPassword) {
      newErrors.currentPassword = "현재 비밀번호를 입력해주세요.";
    }

    if (newPassword && newPassword.length < 6) {
      newErrors.newPassword = "비밀번호는 최소 6자 이상이어야 합니다.";
    }

    if (!nickname) {
      newErrors.nickname = "닉네임은 필수입니다.";
    } else if (nickname.length > 15) {
      newErrors.nickname = "닉네임은 최대 15자까지 가능합니다.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleUpdate = async () => {
    if (!validateInputs()) {
      return;
    }

    try {
      await axios.put("https://www.jsw-resumeandportfolio.com/api/users/update",
          {
            nickname,
            currentPassword,
            newPassword
          }, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
            }
          });
      alert("회원 정보가 수정되었습니다.");
      window.location.href = "/";
    } catch (err) {
      setMessage(err.response?.data?.message || "회원 정보 수정 중 오류 발생");
    }
  };

  return (
      <div className="max-w-md mx-auto mt-10 bg-white p-6 shadow-md rounded-lg">
        <h2 className="text-xl font-bold mb-4">회원 정보 수정</h2>

        {message && <p className="text-red-600 text-sm mb-4">{message}</p>}

        <label className="block text-gray-700 mb-1">닉네임</label>
        <input
            type="text"
            className="w-full px-4 py-2 border rounded-lg mb-1"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            placeholder="닉네임을 입력하세요"
        />
        {errors.nickname && <p
            className="text-red-600 text-sm mb-4">{errors.nickname}</p>}

        <label className="block text-gray-700 mb-1">현재 비밀번호</label>
        <input
            type="password"
            className="w-full px-4 py-2 border rounded-lg mb-1"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            placeholder="현재 비밀번호"
        />
        {errors.currentPassword && <p
            className="text-red-600 text-sm mb-4">{errors.currentPassword}</p>}

        <label className="block text-gray-700 mb-1">새 비밀번호</label>
        <input
            type="password"
            className="w-full px-4 py-2 border rounded-lg mb-1"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="새 비밀번호"
        />
        {errors.newPassword && <p
            className="text-red-600 text-sm mb-4">{errors.newPassword}</p>}

        <button
            onClick={handleUpdate}
            className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700"
        >
          수정하기
        </button>
      </div>
  );
};

export default UpdateProfile;