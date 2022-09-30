import React, { useEffect, useState } from 'react';
import styles from './MyPage.module.css';
import MyTop from '../components/my/MyTop';
import Profile from '../components/my/Profile';
import MyStar from '../components/my/MyStar';
import MyChallenge from '../components/my/MyChallenge';
import ChallengeList from '../components/my/ChallengeList';
import { useNavigate } from 'react-router-dom';

function MyPage() {
  const navigate = useNavigate();
  return (
    <div className='wrapper'>
      <MyTop styles={styles}></MyTop>
      <Profile styles={styles}></Profile>
      <hr></hr>
      <MyStar styles={styles}></MyStar>
      <hr></hr>
      <ChallengeList></ChallengeList>
      <hr></hr>
      <button onClick={() => navigate(`/dashboard`)} className={styles.myWallet}>
        블록체인 대시보드
      </button>
      <button onClick={() => navigate(`/create/deploy`)} className={styles.myWallet}>
        컨트랙트 배포하기
      </button>
      <button onClick={() => navigate(`/create`)} className={styles.myWallet}>
        등록하기
      </button>
    </div>
  );
}

export default MyPage;
