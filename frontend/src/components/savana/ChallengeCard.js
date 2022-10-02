import React, { useEffect, useState, useContext } from 'react';
import ReactPlayer from 'react-player';
import {
  RiZoomInFill,
  RiMessage3Line,
  RiHeart2Fill,
  RiHeart2Line,
  RiShareFill,
  RiBearSmileFill,
} from 'react-icons/ri';
import swal from 'sweetalert';
import Sheet from 'react-modal-sheet';
import SavanaComment from './SavanaComment';
import UseAxios from '../../utils/UseAxios';
import { useNavigate } from 'react-router-dom';

function ProgressBar(props) {
  const [value, setValue] = useState(0);

  useEffect(() => {
    const newValue = props.width * props.percent;
    setValue(newValue);
  }, [props.width, props.percent]);
  return (
    <div className={props.styles.progressDiv} style={{ width: props.width }}>
      <div style={{ width: `${value}px` }} className={props.styles.progress} />
    </div>
  );
}

function ChallengeCard(props) {
  // 마우스 오버 상태
  const [hover, setHover] = useState(false);
  const [copied, setCopied] = useState(false);
  const [isOpen, setOpen] = useState(false);
  const [data, setData] = useState(null);
  const navigate = useNavigate();

  function copy() {
    const el = document.createElement('input');
    el.value = window.location.href;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
    setCopied(true);
    swal('링크 복사가 완료되었습니다');
  }

  useEffect(() => {
    if (props.item) {
      setData(props.item);
    }
  }, [props.item]);

  useEffect(() => {
    if (props.check) {
    }
  }, [props.check]);

  const likeButtonClick = () => {
    if (!data.liked) {
      UseAxios.post(`/challenges/like?challengeId=${data.challengeId}`, {
        params: {
          challengeId: data.challengeId,
        },
      })
        .then((res) => {
          setData((data) => ({
            ...data,
            liked: !data.liked,
            likeCnt: data.likeCnt + 1,
          }));
          console.log('좋아용');
        })
        .catch((err) => {
          console.log(err);
        });
    } else if (data.liked) {
      UseAxios.delete(`/challenges/like?challengeId=${data.challengeId}`, {
        params: {
          challengeId: data.challengeId,
        },
      })
        .then((res) => {
          setData((data) => ({
            ...data,
            liked: !data.liked,
            likeCnt: data.likeCnt - 1,
          }));
          console.log('싫어용');
        })
        .catch((err) => {
          console.log(err);
        });
    }
  };
  const check = () => {
    if (props.check) {
      // props.check.current[props.index + 1].volume = 0.1;
      // props.check.current[props.index + 1].play();
      // if (!props.check.current[props.index].paused) {
      //   props.check.current[props.index].pause();
      // }
      // props.check.current[props.index].onL
    }
  };
  return data ? (
    <div className={props.styles.cardWrapper} onMouseOut={() => setHover(false)} onTouchEnd={check}>
      <div className={props.styles.coverBox}>
        <div className={props.styles.blankBox}></div>

        <div className={props.styles.iconBox}>
          <a
            onClick={() =>
              navigate(`/challenge/${data.challengeId}`, {
                state: {
                  data,
                },
              })
            }
          >
            <RiZoomInFill className={props.styles.clickIcon}></RiZoomInFill>
          </a>
          <div>
            <a onClick={likeButtonClick}>
              {!data.liked ? (
                <RiHeart2Line className={props.styles.clickIcon}></RiHeart2Line>
              ) : (
                <RiHeart2Fill className={props.styles.likeIcon}></RiHeart2Fill>
              )}
            </a>
            <div className={props.styles.iconCount}>{data.likeCnt}</div>
          </div>
          <div>
            <a onClick={() => setOpen(true)} className='button'>
              <RiMessage3Line className={props.styles.clickIcon}></RiMessage3Line>
              <Sheet isOpen={isOpen} onClose={() => setOpen(false)}>
                <Sheet.Container
                  style={{ height: '500px', zIndex: 10000002, position: 'absolute' }}
                >
                  <Sheet.Header />
                  <Sheet.Content style={{ margin: '20px' }}>
                    <SavanaComment></SavanaComment>
                  </Sheet.Content>
                </Sheet.Container>
                <Sheet.Backdrop style={{ zIndex: 10000001 }} />
              </Sheet>
            </a>
            <div className={props.styles.iconCount}>{data.commentCount}</div>
          </div>
          <a>
            <RiShareFill className={props.styles.clickIcon} onClick={copy}></RiShareFill>
          </a>
        </div>
        <div className={props.styles.infoBox}>
          <div className={props.styles.infoTop}>
            <div className={props.styles.infoTitle}>{data.title}</div>
          </div>
          <ProgressBar
            styles={props.styles}
            width={'90vw'}
            percent={data.currentNum / data.targetNum}
          ></ProgressBar>
          <div className={props.styles.infoBot}>
            <span className={props.styles.infoText}>{data.currentNum}명</span>
            <span className={props.styles.infoText}>{data.currentNum / data.targetNum}%</span>
          </div>
        </div>
      </div>
      {/* <ReactPlayer
        className={props.styles.reactPlayer}
        url={`/files/${data.video}`}
        width='100%'
        height='100%'
        playing={hover}
        controls={false}
        volume={0.1}
        playsinline
        ref={(el) => (props.check ? (props.check.current[props.index] = el) : '')}
      /> */}
      <video
        src={`/files/${data.video}`}
        ref={(el) => (props.check ? (props.check.current[props.index] = el) : '')}
        playsInline
        style={{ width: '100%', height: '100%' }}
        onCanPlayThrough={() => console.log('중단없이 재생가능')}
      />
    </div>
  ) : (
    ''
  );
}

export default ChallengeCard;
