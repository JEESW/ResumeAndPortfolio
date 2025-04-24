import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Header from "./components/Header/Header";
import Footer from "./components/Footer/Footer";
import Home from "./pages/Home";
import Resume from "./pages/Resume";
import Portfolio from "./pages/Portfolio";
import Faq from "./pages/Faq";
import Login from "./pages/Login"
import OAuthRedirectHandler from "./components/Social-Login/OAuthRedirectHandler";
import SignUp from "./pages/SignUp";
import UpdateProfile from "./pages/UpdateProfile";

function App() {
  return (
      <Router>
        <Header />
        <main>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/resume" element={<Resume />} />
            <Route path="/portfolio" element={<Portfolio />} />
            <Route path="/faq" element={<Faq />} />
            <Route path="/login" element={<Login />} />
            <Route path="/oauth2/callback" element={<OAuthRedirectHandler />} />
            <Route path="/signup" element={<SignUp />} />
            <Route path="/update-profile" element={<UpdateProfile />} />
          </Routes>
        </main>
        <Footer />
      </Router>
  );
}

export default App;