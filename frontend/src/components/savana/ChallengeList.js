import React, { useContext, useEffect, useRef, useState } from 'react';
import Context from '../../utils/Context';
import ChallengeCard from './ChallengeCard';
// import { isIOS } from 'react-device-detect';
function ChallengeList(props) {
  const idxRef = useRef([]);
  // const audioRef = useRef([]);
  const prevRef = useRef(null);
  const conRef = useRef(null);
  const [touchPosition, setTouchPosition] = useState(null);
  const [distanceY, setdistanceY] = useState(null);
  const [loading, setLoading] = useState(null);
  const [isOpen, setIsOpen] = useState(false);
  const { setSelected } = useContext(Context);

  const height = window.innerHeight - 56;
  useEffect(() => {
    // if (isIOS) {
    //   if (idxRef.current[props.idx] && audioRef.current[props.idx] && loading) {
    //     conRef.current.scrollTo(0, props.idx * height);
    //     audioRef.current[props.idx].volume = 0.1;
    //     idxRef.current[props.idx].play();
    //     audioRef.current[props.idx].play();
    //     if (props.data[props.idx].isProceeding) {
    //       setSelected(props.data[props.idx].challengeId);
    //     } else {
    //       setSelected(false);
    //     }
    //     prevRef.current = props.idx;
    //   }
    // } else {
    if (idxRef.current[props.idx] && loading) {
      conRef.current.scrollTo(0, props.idx * height);
      idxRef.current[props.idx].volume = 0.1;
      idxRef.current[props.idx].play();
      idxRef.current[props.idx].muted = false;
      if (props.data[props.idx].isProceeding) {
        setSelected(props.data[props.idx].challengeId);
      } else {
        setSelected(false);
      }
      prevRef.current = props.idx;
    }
    // }
  }, [loading]);

  const check = (e) => {
    const scrollHeight = e.target.scrollTop;
    const idx = scrollHeight / height;
    if (Number.isInteger(scrollHeight / height)) {
      if (props.data[idx].isProceeding) {
        setSelected(props.data[idx].challengeId);
      } else {
        setSelected(0);
      }
      // if (isIOS) {
      //   if (distanceY > 0 && idx > 0) {
      //     if (idx === props.data.length - 1 && idx === prevRef.current) {
      //       idxRef.current[idx].play();
      //       audioRef.current[idx].volume = 0.1;
      //       audioRef.current[idx].play();
      //     } else {
      //       idxRef.current[idx].play();
      //       audioRef.current[idx].play();
      //       audioRef.current.volume = 0.1;
      //       idxRef.current[prevRef.current].currentTime = 0;
      //       idxRef.current[prevRef.current].pause();
      //       // audioRef.current[prevRef.current].currentTime = 0;
      //       audioRef.current[prevRef.current].pause();
      //       prevRef.current = idx;
      //     }
      //   } else if (distanceY < 0) {
      //     idxRef.current[idx].play();
      //     audioRef.current[idx].volume = 0.1;
      //     audioRef.current[idx].play();
      //     idxRef.current[prevRef.current].currentTime = 0;
      //     idxRef.current[prevRef.current].pause();
      //     // audioRef.current[prevRef.current].currentTime = 0;
      //     audioRef.current[prevRef.current].pause();
      //     prevRef.current = idx;
      //   }
      // } else {
      if (distanceY > 0 && idx > 0) {
        if (idx === props.data.length - 1 && idx === prevRef.current) {
          idxRef.current[idx].volume = 0.1;
          idxRef.current[idx].play();
          idxRef.current[idx].muted = false;
        } else {
          idxRef.current[idx].volume = 0.1;
          idxRef.current[idx].play();
          idxRef.current[idx].muted = false;
          idxRef.current[prevRef.current].currentTime = 0;
          idxRef.current[prevRef.current].pause();
          prevRef.current = idx;
        }
      } else if (distanceY < 0) {
        idxRef.current[idx].volume = 0.1;
        idxRef.current[idx].play();
        idxRef.current[idx].muted = false;
        idxRef.current[prevRef.current].currentTime = 0;
        idxRef.current[prevRef.current].pause();
        prevRef.current = idx;
      }
    }
    // }
  };

  const touchEnd = (e) => {
    setdistanceY(touchPosition.y - e.changedTouches[0].pageY);
  };
  return (
    <div
      className={props.styles.challengeContainer}
      onTouchStart={(e) =>
        setTouchPosition({
          x: e.changedTouches[0].pageX,
          y: e.changedTouches[0].pageY,
        })
      }
      onTouchEnd={touchEnd}
      onScroll={check}
      ref={conRef}
    >
      {props.data
        ? props.data.map((item, index) => (
            <ChallengeCard
              check={idxRef}
              styles={props.styles}
              item={item}
              index={index}
              key={index}
              setLoading={index === 0 ? setLoading : false}
              isOpen={isOpen}
              setIsOpen={setIsOpen}
              // isIOS={isIOS}
              // audio={audioRef}
            />
          ))
        : ''}
    </div>
  );
}

export default ChallengeList;
