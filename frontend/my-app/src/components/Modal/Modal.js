import React from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";

const Modal = ({isOpen, onClose, nickname}) => {
  const navigate = useNavigate();

  if (!isOpen) {
    return null;
  }

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  const handleUpdateNavigate = () => {
    navigate("/update-profile");
  };

  const handleDeleteUser = async () => {
    try {
      await axios.delete(
          "https://www.jsw-resumeandportfolio.com/api/users/delete", {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
            },
          });
      localStorage.removeItem("accessToken");
      alert("회원 탈퇴가 완료되었습니다.");
      window.location.href = "/";
    } catch (err) {
      alert("회원 탈퇴 중 오류가 발생했습니다.");
    }
  };

  return (
      <div
          className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50"
          onClick={handleOverlayClick}
      >
        <div
            className="bg-white rounded-lg overflow-hidden shadow-lg w-80 relative">
          <div className="bg-blue-500 text-white text-center py-4">
            <div className="text-4xl">
              <i className="fas fa-user-circle"></i>
            </div>
            <p className="mt-2 text-lg">{nickname}</p>
          </div>
          <div className="p-6 text-center">
            <button
                className="block w-full py-2 mb-4 border border-gray-300 rounded-lg hover:bg-gray-100"
                onClick={handleUpdateNavigate}
            >
              회원 정보 수정
            </button>
            <button
                className="block w-full py-2 border border-red-300 text-red-600 rounded-lg hover:bg-red-100"
                onClick={handleDeleteUser}
            >
              회원 탈퇴
            </button>
          </div>
          <button
              className="absolute top-4 right-4 text-white hover:text-gray-900"
              onClick={onClose}
          >
            ×
          </button>
        </div>
      </div>
  );
};

export default Modal;