import React, { useContext, useEffect, useState } from 'react';
import styles from './MyPage.module.css';
import MyTop from '../components/my/MyTop';
import Profile from '../components/my/Profile';
import MyStar from '../components/my/MyStar';
import { useNavigate } from 'react-router-dom';
import Category from '../components/common/Category';
import UseAxios from '../utils/UseAxios';
import Context from '../utils/Context';
import ChallengeList from '../components/home/ChallengeList';

function MyPage() {
  const [isParticipated, setIsParticipated] = useState(true);
  const [participatedData, setParticipatedData] = useState(null);
  const [likedData, setLikedData] = useState(null);
  const { userData } = useContext(Context);
  const navigate = useNavigate();

  useEffect(() => {
    if (userData) {
      UseAxios.get(`/challenges/user/${userData.id}`).then((res) => {
        setLikedData(res.data);
      });
      UseAxios.get(`/challenges?scope=all&order=latest&userId=${userData.id}`).then((res) => {
        setParticipatedData(res.data);
      });
    }
  }, [userData]);

  return userData ? (
    <div className='wrapper'>
      <MyTop styles={styles}></MyTop>
      <Profile styles={styles}></Profile>
      <hr></hr>
      <MyStar styles={styles}></MyStar>
      <hr></hr>
      <div className={styles.titleBox}>
        <Category title={'챌린지'}></Category>
        <div className={styles.sortTab}>
          {isParticipated ? (
            <span style={{ color: '#ffc947' }}>참여</span>
          ) : (
            <span onClick={() => setIsParticipated(true)}>참여</span>
          )}
          {isParticipated ? (
            <span onClick={() => setIsParticipated(false)}>좋아요</span>
          ) : (
            <span style={{ color: '#ffc947' }}>좋아요</span>
          )}
        </div>
      </div>
      {isParticipated ? (
        <ChallengeList data={participatedData} category={5}></ChallengeList>
      ) : (
        <ChallengeList data={likedData} category={6}></ChallengeList>
      )}
      <hr></hr>
      <button onClick={() => navigate(`/create/deploy`)} className={styles.myWallet}>
        컨트랙트 배포하기
      </button>
      <button onClick={() => navigate(`/create`)} className={styles.myWallet}>
        등록하기
      </button>
    </div>
  ) : (
    ''
  );
}

export default MyPage;
