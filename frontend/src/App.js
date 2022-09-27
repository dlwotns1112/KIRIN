import './App.css';
import { isMobile } from 'react-device-detect';
import HomePage from './pages/HomePage';
import { Routes, Route } from 'react-router-dom';
import SearchPage from './pages/SearchPage';
import SavanaPage from './pages/SavanaPage';
import DonationPage from './pages/DonationPage';
import StarPage from './pages/StarPage';
import PlusPage from './pages/PlusPage';
import PcPage from './pages/PcPage';
import MyPage from './pages/MyPage';
import SettingPage from './pages/SettingPage';
import EditProfilePage from './pages/settings/EditProfilePage';
import AskPage from './pages/settings/AskPage';
import ChangePasswordPage from './pages/settings/ChangePasswordPage';
import TermsOfServicePage from './pages/settings/TermsOfServicePage';
import PrivacyPolicyPage from './pages/settings/PrivacyPolicyPage';
import NotFoundPage from './pages/NotFoundPage';
import ChallengePage from './pages/ChallengePage';
import SimpleBottomNavigation from './components/common/SimpleBottomNavigation';
import CommunityPage from './pages/CommunityPage';
import BlockchainPage from './pages/BlockchainPage';
import TransactionPage from './pages/TransactionPage';
import DashboardPage from './pages/DashboardPage';
import FindPasswordPage from './pages/FindPasswordPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import FinishSignupPage from './pages/FinishSignupPage';
import ContractDeploy from './pages/ContractDeploy';
import PreviewPage from './pages/PreviewPage';
import React, { useEffect, useState } from 'react';
import Context from './utils/Context';
import UseAxios from './utils/UseAxios';
import { Cookies } from 'react-cookie';
import StarCreatePage from './pages/StarCreatePage';

function App() {
  const [blob, setBlob] = useState(null);
  const [userData, setUserData] = useState(null);
  const cookies = new Cookies();
  const value = cookies.get('accesstoken');
  useEffect(() => {
    if (value) {
      UseAxios.get(`/users/profiles`).then((res) => {
        setUserData(res.data);
      });
    }
  }, []);
  return (
    <>
      {isMobile ? (
        value ? (
          <div className='App'>
            <Context.Provider value={{ blob, setBlob, userData, setUserData }}>
              <Routes>
                <Route path='/' element={<HomePage></HomePage>}></Route>
                <Route path='/search' element={<SearchPage></SearchPage>}></Route>
                <Route path='/savana' element={<SavanaPage></SavanaPage>}></Route>
                <Route path='/donation' element={<DonationPage></DonationPage>}></Route>
                <Route path='/plus' element={<PlusPage></PlusPage>}></Route>
                <Route path='/preview' element={<PreviewPage></PreviewPage>}></Route>
                <Route path='/star/:starId' element={<StarPage></StarPage>}></Route>
                <Route
                  path='/star/:starId/community'
                  element={<CommunityPage></CommunityPage>}
                ></Route>
                <Route
                  path="/challenge/:challengeId"
                  element={<ChallengePage></ChallengePage>}
                ></Route>
                <Route path="*" element={<NotFoundPage></NotFoundPage>}></Route>
                <Route path="/mypage" element={<MyPage></MyPage>}></Route>
                <Route path="/mypage/setting" element={<SettingPage></SettingPage>}></Route>
                <Route path="/dashboard" element={<DashboardPage></DashboardPage>}></Route>
                <Route
                  path='/dashboard/blockchain'
                  element={<BlockchainPage></BlockchainPage>}
                ></Route>
                <Route
                  path='/dashboard/transaction'
                  element={<TransactionPage></TransactionPage>}
                ></Route>
                <Route path='/create/deploy' element={<ContractDeploy></ContractDeploy>}></Route>
                <Route
                  path='/mypage/setting/editprofile'
                  element={<EditProfilePage></EditProfilePage>}
                ></Route>
                <Route
                  path='/mypage/setting/changepassword'
                  element={<ChangePasswordPage></ChangePasswordPage>}
                ></Route>
                <Route path='/mypage/setting/ask' element={<AskPage></AskPage>}></Route>
                <Route
                  path='/mypage/setting/termsofservice'
                  element={<TermsOfServicePage></TermsOfServicePage>}
                ></Route>
                <Route
                  path='/mypage/setting/privacypolicy'
                  element={<PrivacyPolicyPage></PrivacyPolicyPage>}
                ></Route>
                <Route path='/mypage/wallet' element={<WalletPage></WalletPage>}></Route>
                <Route path='/signup' element={<SignupPage></SignupPage>}></Route>
                <Route path='/login' element={<LoginPage></LoginPage>}></Route>
                <Route path='/finishsignup' element={<FinishSignupPage></FinishSignupPage>}></Route>
                <Route path='/findpassword' element={<FindPasswordPage></FindPasswordPage>}></Route>
                <Route path='/create' element={<StarCreatePage></StarCreatePage>}></Route>
              </Routes>
              <SimpleBottomNavigation></SimpleBottomNavigation>
            </Context.Provider>
          </div>
        ) : (
          <div className='App'>
            <Context.Provider value={{ blob, setBlob, userData, setUserData }}>
              <Routes>
                <Route path='/' element={<LoginPage></LoginPage>}></Route>
                <Route path='/finishsignup' element={<FinishSignupPage></FinishSignupPage>}></Route>
                <Route path='/findpassword' element={<FindPasswordPage></FindPasswordPage>}></Route>
                <Route path='/signup' element={<SignupPage></SignupPage>}></Route>
                <Route path='*' element={<NotFoundPage></NotFoundPage>}></Route>
              </Routes>
            </Context.Provider>
          </div>
        )
      ) : (
        <div className='Pc'>
          <PcPage></PcPage>
        </div>
      )}
    </>
  );
}

export default App;
